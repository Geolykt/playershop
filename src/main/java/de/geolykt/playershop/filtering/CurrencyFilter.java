package de.geolykt.playershop.filtering;

import java.util.UUID;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import de.geolykt.playercurrency.PlayerCurrency;
import de.geolykt.playershop.Translator;
import de.geolykt.playershop.listing.ShopListing;

public class CurrencyFilter implements Filter {

    public static enum Type {
        ALL_OFFERS(Translator.ALL_OFFERS),
        OWNED_ONLY(Translator.FILTER_OWNED_CURRENCY_ONLY);

        @NotNull
        private static final Type[] TYPES = values(); // default implementation of #values uses a clone operation, which has a performance impact

        @NotNull
        private final String translationKey;

        Type(@NotNull String translationKey) {
            this.translationKey = translationKey;
        }

        @NotNull
        public Type cycle() {
            return TYPES[(this.ordinal() + 1) % TYPES.length];
        }

        @NotNull
        public String getTranslationKey() {
            return translationKey;
        }
    }

    @NotNull
    private Type type;

    @NotNull
    private final UUID userUUID;

    public CurrencyFilter(@NotNull Type type, @NotNull UUID userUUID) {
        this.type = type;
        this.userUUID = userUUID;
    }

    @Override
    public boolean allows(@NotNull ShopListing listing) {
        if (type == Type.ALL_OFFERS) {
            return true;
        } else {
            return PlayerCurrency.getInstance().getBalance(userUUID, listing.getCurrency()) > 0;
        }
    }

    @NotNull
    public Type getType() {
        return type;
    }

    public void setType(@NotNull Type type) {
        this.type = type;
    }

    @Override
    public void cycleModes() {
        this.type = this.type.cycle();
    }

    @Override
    @NotNull
    public String getModeTranslationKey() {
        return this.type.translationKey;
    }

    @Override
    @NotNull
    public Material getIcon() {
        if (type == Type.ALL_OFFERS) {
            return Material.GREEN_WOOL;
        } else if (type == Type.OWNED_ONLY) {
            return Material.GOLD_BLOCK;
        } else {
            return Material.BARRIER;
        }
    }
}
