package de.geolykt.playershop.filtering;

import java.util.UUID;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import de.geolykt.playershop.Translator;
import de.geolykt.playershop.listing.ShopListing;

public class OffererFilter implements Filter {

    public static enum Type {
        ALL_OFFERS(Translator.ALL_OFFERS),
        NO_SELF(Translator.FILTER_NO_SELF),
        ONLY_SELF(Translator.FILTER_ONLY_SELF);

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

    public OffererFilter(@NotNull Type type, @NotNull UUID userUUID) {
        this.type = type;
        this.userUUID = userUUID;
    }

    @Override
    public boolean allows(@NotNull ShopListing listing) {
        if (type == Type.ALL_OFFERS) {
            return true;
        } else if (type == Type.NO_SELF) {
            return !listing.offerer().equals(userUUID);
        } else if (type == Type.ONLY_SELF) {
            return listing.offerer().equals(userUUID);
        } else {
            throw new IllegalStateException();
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
        } else if (type == Type.ONLY_SELF) {
            return Material.GOLD_BLOCK;
        } else if (type == Type.NO_SELF) {
            return Material.REDSTONE_BLOCK;
        } else {
            return Material.BARRIER;
        }
    }
}
