package de.geolykt.playershop;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class Translator {

    public static final Map<String, Map<String, String>> TRANSLATIONS = new HashMap<>();
    private static final Map<String, String> EN = new HashMap<>();
    private static final Map<String, String> DE = new HashMap<>();
    private static final Map<String, String> DEFAULT = EN;

    @NotNull
    public static final String WRONG_ARGUMENT_COUNT_CORRECTED_SYNTAX = "wrong-arg-count";
    @NotNull
    public static final String REQUIRED_ABBREVIATION_OR_NAME = "required.abbrev-or-name";
    @NotNull
    public static final String CURRENCY_NOT_FOUND = "currency-not-found";
    @NotNull
    public static final String REQUIRED_ABBREVIATION = "require.abbrev";
    @NotNull
    public static final String REQUIRED_NAME = "require.name";
    @NotNull
    public static final String ABBREVIATION_PRESENT = "abbrev.present";
    @NotNull
    public static final String NAME_PRESENT = "name.present";
    @NotNull
    public static final String CURRENCY_CREATED = "currency.created.success";
    @NotNull
    public static final String REQUIRED_AMOUNT = "require.amount";
    @NotNull
    public static final String REQUIRED_PLAYER = "require.player";
    @NotNull
    public static final String ERROR_INVALID_AMOUNT = "error.invalid-amount";
    @NotNull
    public static final String NOT_ENOUGH_MONEY = "error.not-enough-money";
    @NotNull
    public static final String NAME_LENGTH_INVALID = "error.param.abbrev-or-name.length";
    @NotNull
    public static final String ILLEGAL_CHARACTER = "error.param.abbrev-or-name.illegalchar";
    @NotNull
    public static final String PLAYER_UNKNOWN = "error.param.player.unknown";
    @NotNull
    public static final String NO_BALANCES = "balances.none";
    @NotNull
    public static final String REQUIRED_CURRENCY = "required.curr";
    @NotNull
    public static final String PAY_TRANSACTION_SUCCESS = "pay.send";
    @NotNull
    public static final String RECIEVED_MONEY = "pay.recieve";
    @NotNull
    public static final String ALL_OFFERS = "filter.all";
    @NotNull
    public static final String FILTER_NO_SELF = "filter.no-self";
    @NotNull
    public static final String FILTER_ONLY_SELF = "filter.only-self";
    @NotNull
    public static final String FILTER_OWNED_CURRENCY_ONLY = "filter.currency.owned";
    @NotNull
    public static final String FILTER_PAYABLE = "filter.payable";
    @NotNull
    public static final String FILTER_UNPAYABLE = "filter.unpayable";
    @NotNull
    public static final String SHOP_MAIN_TITLE = "shop.main-title";
    @NotNull
    public static final String CONFIGURE_FILTERS = "shop.filter.configure";
    @NotNull
    public static final String RESET_FILTERS = "shop.filter.reset";
    @NotNull
    public static final String NEXT_PAGE = "shop.page.next";
    @NotNull
    public static final String PREVIOUS_PAGE = "shop.page.previous";
    @NotNull
    public static final String TITLE_CURRENCY_FILTERING = "settings.currency";
    @NotNull
    public static final String TITLE_PAYABILLITY_FILTERING = "settings.payabillity";
    @NotNull
    public static final String TITLE_OFFERER_FILTERING = "settings.offerer";
    @NotNull
    public static final String COST = "cost";
    @NotNull
    public static final String SHOP_SELL_TITLE = "shop.sell-title";
    @NotNull
    public static final String AFFIRM = "ok";
    @NotNull
    public static final String GIVE_PRICE_PROMPT = "prompt.price";
    @NotNull
    public static final String ITEMS_LISTED = "listing.listed";
    @NotNull
    public static final String UNPARSABLE_AMOUNT = "prompt.price.unparsable";
    @NotNull
    public static final String INVALID_LISTING = "listing.unlisted";
    @NotNull
    public static final String BOUGHT_ITEM = "listing.buy";
    @NotNull
    public static final String SOLD_ITEM = "listing.sold";

    @NotNull
    public static String translate(@NotNull String key, @NotNull Locale locale) {
        Map<String, String> translationTable = TRANSLATIONS.getOrDefault(locale.getISO3Language(), DEFAULT);
        String value = translationTable.get(key);
        if (value == null) {
            PlayerShop.getInstance().getSLF4JLogger().warn("Unable to find value for translation key \"{}\" in language table \"{}\".", key, locale.getISO3Language());
            String s = DEFAULT.getOrDefault(key, key);
            if (s == null) {
                return "";
            }
            return s;
        }
        return value;
    }

    static {
        TRANSLATIONS.put(Locale.ENGLISH.getISO3Language(), EN);
        TRANSLATIONS.put(Locale.GERMAN.getISO3Language(), DE);

        EN.put(WRONG_ARGUMENT_COUNT_CORRECTED_SYNTAX, "Syntax error: Wrong argument count. Correct command syntax: ");
        DE.put(WRONG_ARGUMENT_COUNT_CORRECTED_SYNTAX, "Falsche Anzahl von Befehlsparametern. Richtige syntax: ");

        EN.put(REQUIRED_ABBREVIATION_OR_NAME, "[abbreviation|name]");
        DE.put(REQUIRED_ABBREVIATION_OR_NAME, "[Kürzel|Name]");

        EN.put(CURRENCY_NOT_FOUND, "Currency not found (Did you perform any typographical errors?): ");
        DE.put(CURRENCY_NOT_FOUND, "Währung konnte nicht gefunden werden: ");

        EN.put(REQUIRED_ABBREVIATION, "[abbreviation]");
        DE.put(REQUIRED_ABBREVIATION, "[Kürzel]");

        EN.put(REQUIRED_NAME, "[name]");
        DE.put(REQUIRED_NAME, "[Name]");

        EN.put(ABBREVIATION_PRESENT, "There is already a currency with this abbreviation.");
        DE.put(ABBREVIATION_PRESENT, "Eine Währung hat bereits dieses Kürzel.");

        EN.put(NAME_PRESENT, "There is already a currency going by this name.");
        DE.put(NAME_PRESENT, "Es gibt schon eine Währung mit diesen Namen.");

        EN.put(CURRENCY_CREATED, "Successfully created your currency.");
        DE.put(CURRENCY_CREATED, "Währung erfolgreich erstellt.");

        EN.put(REQUIRED_AMOUNT, "[amount]");
        DE.put(REQUIRED_AMOUNT, "[Menge]");

        EN.put(REQUIRED_PLAYER, "[player]");
        DE.put(REQUIRED_PLAYER, "[Spieler]");

        EN.put(ERROR_INVALID_AMOUNT, "Could not parse your amount. Note that only interger amounts are allowed. You used: ");
        DE.put(ERROR_INVALID_AMOUNT, "Ungültige Menge. Beachte, dass nur Ganzzahlen gültig sind. Verwendet wurde: ");

        EN.put(NOT_ENOUGH_MONEY, "You do not have enough money in your bank to complete this transaction.");
        DE.put(NOT_ENOUGH_MONEY, "Es gibt nicht genug Geld in der Kasse, um diese Transaktion zu gewährleisten.");

        EN.put(NAME_LENGTH_INVALID, "The name of a currency must be over 3 letters long. And the abbreviation must be equal to 3 letters long.");
        DE.put(NAME_LENGTH_INVALID, "Der Kürzel einer Währung muss 3 Zeichen lang sein, der Name länger.");

        EN.put(ILLEGAL_CHARACTER, "The name or abbreviation of the currency contains an illegal character. Consider using standard english letters.");
        DE.put(ILLEGAL_CHARACTER, "Der Name oder Kürzel der Wärhung enthält ein ungültiges Zeichen.");

        EN.put(PLAYER_UNKNOWN, "The requested player has never played on this server. Perhaps you did a typo?");
        DE.put(PLAYER_UNKNOWN, "Dieser Spieler hat sich nie in diesem Server eingeloggt.");

        EN.put(NO_BALANCES, "This player has no money at all.");
        DE.put(NO_BALANCES, "Dieser Spieler hat gar kein Geld.");

        EN.put(REQUIRED_CURRENCY, "[currency]");
        DE.put(REQUIRED_CURRENCY, "[Währung]");

        EN.put(PAY_TRANSACTION_SUCCESS, "Transaction successfull.");
        DE.put(PAY_TRANSACTION_SUCCESS, "Transaktion erfolgreich.");

        // This string is formatted, see https://docs.oracle.com/en/java/javase/16/docs/api/java.base/java/util/Formatter.html
        // index 1 = sender name
        // index 2 = amount
        // index 3 = currency shorthand
        EN.put(RECIEVED_MONEY, "You recieved %2$d %3$s from %1$s");
        DE.put(RECIEVED_MONEY, "%2$d %3$s wurde von %1$s auf das Konto übertragen.");

        EN.put(ALL_OFFERS, "All offers");
        DE.put(ALL_OFFERS, "Alle Angebote");

        EN.put(FILTER_NO_SELF, "No self");
        DE.put(FILTER_NO_SELF, "Keine Eigene");

        EN.put(FILTER_ONLY_SELF, "Only self");
        DE.put(FILTER_ONLY_SELF, "Nur Eigene");

        EN.put(FILTER_OWNED_CURRENCY_ONLY, "Only owned currencies");
        DE.put(FILTER_OWNED_CURRENCY_ONLY, "Nur im Besitz befindliche Währungen");

        EN.put(FILTER_UNPAYABLE, "Only unpayable");
        DE.put(FILTER_UNPAYABLE, "Nur Unbezahlbare");

        EN.put(FILTER_PAYABLE, "Only payable");
        DE.put(FILTER_PAYABLE, "Nur Bezahlbare");

        EN.put(SHOP_MAIN_TITLE, "Shop");
        DE.put(SHOP_MAIN_TITLE, "Shop");

        EN.put(CONFIGURE_FILTERS, "Configure filters");
        DE.put(CONFIGURE_FILTERS, "Filter konfigurieren");

        EN.put(RESET_FILTERS, "Clear all Filters");
        DE.put(RESET_FILTERS, "Alle Filter zurücksetzen");

        EN.put(PREVIOUS_PAGE, "Previous page (%prevpage%)");
        DE.put(PREVIOUS_PAGE, "Vorherige Seite (%prevpage%)");

        EN.put(NEXT_PAGE, "Next page (%nextpage%)");
        DE.put(NEXT_PAGE, "Nächste Seite (%nextpage%)");

        EN.put(TITLE_CURRENCY_FILTERING, "Currency filtering");
        DE.put(TITLE_CURRENCY_FILTERING, "Währungsfilter");

        EN.put(TITLE_OFFERER_FILTERING, "Offerer filtering");
        DE.put(TITLE_OFFERER_FILTERING, "Anbieterfilter");

        EN.put(TITLE_PAYABILLITY_FILTERING, "Payabillity filtering");
        DE.put(TITLE_PAYABILLITY_FILTERING, "Bezahlbarkeitsfilter");

        EN.put(COST, "Cost: %1$d %2$s");
        DE.put(COST, "Preis: %1$d %2$s");

        EN.put(SHOP_SELL_TITLE, "Sell items");
        DE.put(SHOP_SELL_TITLE, "Gegenstände Verkaufen");

        EN.put(AFFIRM, "OK");
        DE.put(AFFIRM, "OK");

        EN.put(GIVE_PRICE_PROMPT, "For how much do you want to sell EACH stack? (Answer in chat - will not be sent publicly)");
        DE.put(GIVE_PRICE_PROMPT, "Für wie viel soll JEWEILS EIN Stack verkauft werden? (Antwort im Chat - wird vorher abgefangen)");

        EN.put(ITEMS_LISTED, "Items successfully listed to the shop!");
        DE.put(ITEMS_LISTED, "Die Items werden nun im Shop angeboten.");

        EN.put(UNPARSABLE_AMOUNT, "Unable to parse prompt input. Make sure you have both an integer currency amount and a valid currency name. (You can try again)");
        DE.put(UNPARSABLE_AMOUNT, "Der Server ist nicht im Stande die Eingabe zu Bewerten. Es muss eine Geldmenge und eine Währung (als Name oder Kürzel) vorliegen. (Es kann ein weiterer Versuch erfolgen)");

        EN.put(INVALID_LISTING, "This Item is no longer available on the market.");
        DE.put(INVALID_LISTING, "Dieser Gegenstand ist nicht mehr Verfügbar.");

        EN.put(BOUGHT_ITEM, "You successfully bought this item for %d %s.");
        DE.put(BOUGHT_ITEM, "Das Item wurde für %d %s gekaufte.");

        EN.put(SOLD_ITEM, "%s bought an item on the shop for %d %s.");
        DE.put(SOLD_ITEM, "%s hat ein Item im Shop für %d %s gekauft.");
    }
}
