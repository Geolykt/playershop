package de.geolykt.playershop.filtering;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import de.geolykt.playershop.listing.ShopListing;

public class FilterStates {

    @NotNull
    public final CurrencyFilter currency;
    @NotNull
    public final OffererFilter offerer;
    @NotNull
    public final PayabillityFilter payabillity;

    public FilterStates(@NotNull UUID user) {
        this.currency = new CurrencyFilter(CurrencyFilter.Type.ALL_OFFERS, user);
        this.offerer = new OffererFilter(OffererFilter.Type.ALL_OFFERS, user);
        this.payabillity = new PayabillityFilter(PayabillityFilter.Type.ALL_OFFERS, user);
    }

    public void reset() {
        this.currency.setType(CurrencyFilter.Type.ALL_OFFERS);
        this.offerer.setType(OffererFilter.Type.ALL_OFFERS);
        this.payabillity.setType(PayabillityFilter.Type.ALL_OFFERS);
    }

    public boolean allows(@NotNull ShopListing listing) {
        return this.currency.allows(listing) && this.offerer.allows(listing) && this.payabillity.allows(listing);
    }
}
