package de.geolykt.playershop.filtering;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import de.geolykt.playershop.listing.ShopListing;

public interface Filter {

    public boolean allows(@NotNull ShopListing listing);

    public void cycleModes();

    @NotNull
    public String getModeTranslationKey();

    @NotNull
    public Material getIcon();
}
