package de.geolykt.playershop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import de.geolykt.playercurrency.Currency;
import de.geolykt.playercurrency.PlayerCurrency;
import de.geolykt.playershop.filtering.Filter;
import de.geolykt.playershop.filtering.FilterStates;
import de.geolykt.playershop.listing.ListingType;
import de.geolykt.playershop.listing.ShopListing;
import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.GuiPageElement.PageAction;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;

import io.papermc.paper.event.player.AsyncChatEvent;

public class PlayerShop extends JavaPlugin {

    private class PlayerShopListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onChat(AsyncChatEvent evt) {
            UUID player = evt.getPlayer().getUniqueId();
            if (!sellInventories.containsKey(player)) {
                return;
            }
            evt.setCancelled(true);
            boolean success = false;
            boolean currencyParseableButUnknown = false;
            String parsedCurrency = "";
            abort: if (evt.message() instanceof TextComponent text) {
                String[] content = text.content().split(" ");
                if (content.length != 2) {
                    break abort;
                }
                try {
                    long amount = Long.parseLong(content[0]);
                    parsedCurrency = content[1];
                    if (parsedCurrency == null) {
                        break abort; // Unlikely to happen, but annotations demand it
                    }
                    Currency c = PlayerCurrency.getInstance().getCurrency(parsedCurrency);
                    if  (c == null) {
                        currencyParseableButUnknown = true;
                        break abort;
                    }
                    for (ItemStack is : sellInventories.remove(player)) {
                        if (is == null) {
                            continue;
                        }
                        listings.add(new ShopListing(c, amount, is, ListingType.SELL, player, new AtomicBoolean(true)));
                    }
                    success = true;
                } catch (NumberFormatException ignored) {}
            }

            Locale lang = evt.getPlayer().locale();
            if (success) {
                evt.getPlayer().sendMessage(Component.text(Translator.translate(Translator.ITEMS_LISTED, lang), NamedTextColor.GREEN));
            } else if (currencyParseableButUnknown) {
                if (parsedCurrency == null) {
                    throw new IllegalStateException();
                }

                evt.getPlayer().sendMessage(Component.text(Translator.translate(Translator.CURRENCY_NOT_FOUND, lang), NamedTextColor.RED).append(Component.text(parsedCurrency, NamedTextColor.DARK_RED)));
            } else {
                evt.getPlayer().sendMessage(Component.text(Translator.translate(Translator.UNPARSABLE_AMOUNT, lang), NamedTextColor.RED));
            }
        }
    }

    private static PlayerShop instance;
    private final Queue<ShopListing> listings = new ConcurrentLinkedDeque<>();
    // TODO A better solution to what we currently have (save this map permanently) would be to have a persistent invalid item
    // queue where the items are put into from which the player can redeem his items back
    // TODO make items expire
    private final Map<UUID, Inventory> sellInventories = new ConcurrentHashMap<>();
    private final Map<UUID, FilterStates> filters = new ConcurrentHashMap<>();
    private boolean loadedSuccess = false;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PlayerShopListener(), this);
        getDataFolder().mkdirs(); // Spigot does not create this folder by default
        readListings: {
            File listingsFile = new File(getDataFolder(), "listings.dat");
            if (!listingsFile.exists()) {
                break readListings;
            }
            try (BukkitObjectInputStream dataIn = new BukkitObjectInputStream(new FileInputStream(listingsFile))) {
                while (dataIn.read() > 0) {
                    Currency curr = PlayerCurrency.getInstance().getCurrency(new UUID(dataIn.readLong(), dataIn.readLong()));
                    long price = dataIn.readLong();
                    ItemStack item = (ItemStack) dataIn.readObject();
                    ListingType type = ListingType.getValuesCached()[dataIn.readByte()];
                    UUID offerer = new UUID(dataIn.readLong(), dataIn.readLong());
                    if (curr == null) {
                        getLogger().warning("Skipping shop listing because a currency apparently no longer exists.");
                        continue;
                    }
                    if (item == null) {
                        getLogger().warning("BukkitObjectInputStream deserialised a null object from stream. Skipping listing");
                        continue;
                    }
                    listings.add(new ShopListing(curr, price, item, type, offerer, new AtomicBoolean(true)));
                }
            } catch (IOException | ClassNotFoundException ex) {
                throw new IllegalStateException("Unable to load plugin data. (In specific the listings.dat file)", ex);
            }
        }
        readSellInventories: {
            File sellInventoriesFile = new File(getDataFolder(), "sellInventories.dat");
            if (!sellInventoriesFile.exists()) {
                break readSellInventories;
            }
            try (BukkitObjectInputStream dataIn = new BukkitObjectInputStream(new FileInputStream(sellInventoriesFile))) {
                while (dataIn.read() > 0) {
                    UUID user = new UUID(dataIn.readLong(), dataIn.readLong());
                    ItemStack[] is = new ItemStack[dataIn.readShort()];
                    for (int i = 0; i < is.length; i++) {
                        is[i] = (ItemStack) dataIn.readObject();
                    }
                    Inventory inv = Bukkit.createInventory(null, is.length);
                    inv.setContents(is);
                    sellInventories.put(user, inv);
                }
            } catch (IOException | ClassNotFoundException ex) {
                throw new IllegalStateException("Unable to load plugin data. (In specific the sellInventories.dat file)", ex);
            }
        }
        loadedSuccess = true;
    }

    @Override
    public void onDisable() {
        if (loadedSuccess) {
            // Do not overwrite with potentially corrupted data
            try (BukkitObjectOutputStream dataOut = new BukkitObjectOutputStream(new FileOutputStream(new File(getDataFolder(), "listings.dat")))) {
                for (ShopListing listing : listings) {
                    if (!listing.valid().getAcquire()) {
                        continue;
                    }
                    dataOut.writeByte(1);
                    dataOut.writeLong(listing.currency().id().getMostSignificantBits());
                    dataOut.writeLong(listing.currency().id().getLeastSignificantBits());
                    dataOut.writeLong(listing.amount());
                    dataOut.writeObject(listing.item());
                    dataOut.writeByte(listing.type().ordinal());
                    dataOut.writeLong(listing.offerer().getMostSignificantBits());
                    dataOut.writeLong(listing.offerer().getLeastSignificantBits());
                }
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to save plugin data. (In specific the listings.dat file)", ex);
            }
            try (BukkitObjectOutputStream dataOut = new BukkitObjectOutputStream(new FileOutputStream(new File(getDataFolder(), "sellInventories.dat")))) {
                for (Map.Entry<UUID, Inventory> entry : sellInventories.entrySet()) {
                    dataOut.writeByte(1);
                    dataOut.writeLong(entry.getKey().getMostSignificantBits());
                    dataOut.writeLong(entry.getKey().getLeastSignificantBits());
                    ItemStack[] contents = entry.getValue().getContents();
                    if (contents == null) {
                        throw new IllegalStateException();
                    }
                    dataOut.writeShort(contents.length);
                    for (int i = 0; i < contents.length; i++) {
                        dataOut.writeObject(contents[i]);
                    }
                }
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to save plugin data. (In specific the sellInventories.dat file)", ex);
            }
        }
    }

    public static PlayerShop getInstance() {
        return instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof @NotNull Player player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player in order to do this!");
            return true;
        }
        showShop(player);
        return true;
    }

    @NotNull
    private GuiElement createFilterItem(@NotNull Locale lang, char slotChar, @NotNull Filter filter, @NotNull String filterTranslationKey) {
        return new DynamicGuiElement(slotChar, viewer -> {
            return new StaticGuiElement(slotChar, new ItemStack(filter.getIcon()), click -> {
                filter.cycleModes();
                click.getGui().draw();
                return true;
            }, ChatColor.RESET + ChatColor.BOLD.toString() + Translator.translate(filterTranslationKey, lang),
            ChatColor.RESET + ChatColor.BOLD.toString() + Translator.translate(filter.getModeTranslationKey(), lang));
        });
    }

    private void showFilterSettings(@NotNull Player player) {
        Locale lang = player.locale();
        InventoryGui settingsGUI = new InventoryGui(this, player, Translator.translate(Translator.CONFIGURE_FILTERS, lang), new String[] {
                "abcdefghi",
                "jklmnopqr",
                "stuvwxyz0"
        });
        settingsGUI.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS, 1));
        FilterStates state = filters.get(player.getUniqueId());

        settingsGUI.addElement(createFilterItem(lang, 'a', state.currency, Translator.TITLE_CURRENCY_FILTERING));
        settingsGUI.addElement(createFilterItem(lang, 'b', state.offerer, Translator.TITLE_OFFERER_FILTERING));
        settingsGUI.addElement(createFilterItem(lang, 'c', state.payabillity, Translator.TITLE_PAYABILLITY_FILTERING));

        settingsGUI.setCloseAction(close -> true);
        settingsGUI.build(player);
        settingsGUI.show(player);
    }

    private void showShop(@NotNull Player player) {
        if (!filters.containsKey(player.getUniqueId())) {
            filters.put(player.getUniqueId(), new FilterStates(player.getUniqueId()));
        }
        Locale lang = player.locale();
        InventoryGui shopGui = new InventoryGui(this, player, Translator.translate(Translator.SHOP_MAIN_TITLE, lang), new String[] {
                "aaaaaaaaa",
                "aaaaaaaaa",
                "-0123456+"
        });
        shopGui.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS, 1));
        shopGui.addElement(new GuiPageElement('-', new ItemStack(Material.OAK_SIGN), PageAction.PREVIOUS, ChatColor.RESET + ChatColor.BOLD.toString() + Translator.translate(Translator.PREVIOUS_PAGE, lang)));
        shopGui.addElement(new GuiPageElement('+', new ItemStack(Material.OAK_SIGN), PageAction.NEXT, ChatColor.RESET + ChatColor.BOLD.toString() +  Translator.translate(Translator.NEXT_PAGE, lang)));

        shopGui.addElement(new StaticGuiElement('0', new ItemStack(Material.COMPASS, 1), click -> {
            click.getWhoClicked().closeInventory(Reason.OPEN_NEW);
            Bukkit.getScheduler().runTask(this, () -> {
                showFilterSettings(player);
            });
            return true;
        },ChatColor.RESET + ChatColor.BOLD.toString() + Translator.translate(Translator.CONFIGURE_FILTERS, lang)));
        shopGui.addElement(new StaticGuiElement('1', new ItemStack(Material.SPONGE, 1), click -> {
            filters.get(player.getUniqueId()).reset();
            return true;
        }, ChatColor.RESET + ChatColor.BOLD.toString() + Translator.translate(Translator.RESET_FILTERS, lang)));
        shopGui.addElement(new StaticGuiElement('2', new ItemStack(Material.GOLD_INGOT, 1), click -> {
            click.getWhoClicked().closeInventory(Reason.OPEN_NEW);
            Bukkit.getScheduler().runTask(this, () -> {
                showSellInventory(player);
            });
            return true;
        }, ChatColor.RESET + ChatColor.BOLD.toString() + Translator.translate(Translator.SHOP_SELL_TITLE, lang)));

        GuiElementGroup offers = new GuiElementGroup('a');
        shopGui.addElement(offers);
        addOffers(player, offers);

        shopGui.build(player);
        shopGui.show(player);
    }

    private void showSellInventory(@NotNull Player player) {
        Locale lang = player.locale();
        InventoryGui sellGUI = new InventoryGui(this, player, Translator.translate(Translator.SHOP_SELL_TITLE, lang), new String[] {
                "abcdefghi",
                "jklmnopqr",
                "stuvwxyz0"
        });

        Inventory sellInv = Bukkit.createInventory(null, 27);

        sellGUI.addElement(new StaticGuiElement('0', new ItemStack(Material.LAPIS_BLOCK, 1), click -> {
            click.getWhoClicked().closeInventory(Reason.PLUGIN); // Also calls the close action
            return true;
        }, ChatColor.RESET + ChatColor.BOLD.toString() + Translator.translate(Translator.AFFIRM, lang)));

        for (char i = 'a'; i <= 'z'; i++) {
            GuiStorageElement elem = new GuiStorageElement(i, sellInv, i - 'a');
            sellGUI.addElement(elem);
        }

        sellGUI.setCloseAction(close -> {
            initSell(sellInv, player);
            return false;
        });

        sellGUI.build(player);
        sellGUI.show(player);
    }

    private void initSell(@NotNull Inventory sellInv, @NotNull Player seller) {
        Inventory oldInv = sellInventories.put(seller.getUniqueId(), sellInv);
        if (oldInv != null) {
            Location loc = seller.getLocation();
            ItemStack[] contents = Objects.requireNonNull(oldInv.getStorageContents());
            for (ItemStack is : contents) {
                if (is == null) {
                    continue;
                }
                seller.getWorld().dropItem(loc, is);
            }
        }
        Locale lang = seller.locale();
        seller.sendMessage(Component.text(Translator.translate(Translator.GIVE_PRICE_PROMPT, lang), NamedTextColor.LIGHT_PURPLE));
    }

    private void addOffers(@NotNull Player player, @NotNull GuiElementGroup offers) {
        Locale locale = player.locale();
        for (ShopListing listing : listings) {
            if (!listing.valid().getAcquire()) {
                continue;
            }
            // TODO deal with custom lore
            offers.addElement(new StaticGuiElement('x', listing.item(), click -> {
                if (listing.amount() > PlayerCurrency.getInstance().getBalance(player.getUniqueId(), listing.getCurrency())) {
                    // Player cannot afford this
                    player.sendMessage(Component.text(Translator.translate(Translator.NOT_ENOUGH_MONEY, locale), NamedTextColor.RED));
                    return true;
                }
                if (!listing.valid().getAndSet(false)) {
                    // We do not remove invalid listings from the list of listings due to performance and concurrency concerns
                    // Listing does not longer exist
                    player.sendMessage(Component.text(Translator.translate(Translator.INVALID_LISTING, locale), NamedTextColor.RED));
                    return true;
                }
                PlayerCurrency.getInstance().addBalance(listing.getOfferer(), listing.getCurrency(), listing.amount(), "Sold Item");
                PlayerCurrency.getInstance().addBalance(player.getUniqueId(), listing.getCurrency(), -listing.amount(), "Bought Item");
                Map<Integer, ItemStack> surplus = player.getInventory().addItem(listing.getListedItem());
                Location loc = player.getLocation();
                surplus.forEach((slot, item) -> {
                    if (item == null) {
                        return; // Wha?
                    }
                    player.getWorld().dropItem(loc, item);
                });

                String buyMessage = Translator.translate(Translator.BOUGHT_ITEM, locale).formatted(listing.amount(), listing.currency().abbreviation());
                if (buyMessage != null) {
                    player.sendMessage(Component.text(buyMessage, NamedTextColor.GREEN));
                }

                Player vendor = Bukkit.getPlayer(listing.getOfferer());
                if (vendor != null && vendor.isOnline()) {
                    String sellMessage = Translator.translate(Translator.SOLD_ITEM, locale).formatted(player.getName(), listing.amount(), listing.currency().abbreviation());
                    if (sellMessage != null) {
                        player.sendMessage(Component.text(sellMessage, NamedTextColor.GREEN));
                    }
                }
                return true;
            }, null, ChatColor.RESET + Translator.translate(Translator.COST, locale).formatted(listing.amount(), listing.getCurrency().abbreviation())));
        }
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
