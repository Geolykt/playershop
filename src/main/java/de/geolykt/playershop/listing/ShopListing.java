package de.geolykt.playershop.listing;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import de.geolykt.playercurrency.Currency;

public record ShopListing(@NotNull Currency currency, long amount, @NotNull ItemStack item, @NotNull ListingType type, @NotNull UUID offerer, @NotNull AtomicBoolean valid) {

    public @NotNull Currency getCurrency() {
        return this.currency;
    }

    public @NotNull ItemStack getListedItem() {
        return item;
    }

    public @NotNull UUID getOfferer() {
        return offerer;
    }
}
