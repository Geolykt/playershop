package de.geolykt.playershop.listing;

import org.jetbrains.annotations.NotNull;

public enum ListingType {
    BUY,
    SELL;

    @NotNull
    private static final ListingType[] VALUES = values();

    @NotNull
    public static ListingType[] getValuesCached() { // #values clones the value array, which is useless memory expense
        return VALUES;
    }
}
