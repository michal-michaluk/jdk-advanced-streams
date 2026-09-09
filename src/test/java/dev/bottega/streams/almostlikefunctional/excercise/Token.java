package dev.bottega.streams.almostlikefunctional.excercise;

/**
 * Sealed token model: exactly four immutable token kinds, known at compile time.
 * <p>
 * Every accessor that "looks inside" a token uses <b>pattern matching by type</b> —
 * a {@code switch} over the sealed hierarchy with type patterns and record
 * (deconstruction) patterns. Because the hierarchy is sealed and the switch is
 * exhaustive, no {@code default} is required and no runtime type check escapes.
 */
public sealed interface Token {

    /** Physical payment card. */
    record Card(String rfid, String visualNumber, String holder) implements Token {}

    /** Mobile application push token. */
    record MobileApp(String deviceId, String pushToken, Platform platform) implements Token {}

    /** Credit card PAN + expiry. */
    record CreditCard(String pan, String expiryDate) implements Token {}

    /** IoT device modem (IMSI / ICCID). */
    record DeviceModem(String imsi, String iccid, String serial) implements Token {}

    enum Platform { ANDROID, IOS }

    /** Classification of a token, including the synthetic {@link #PARSE_ERROR} bucket. */
    enum Kind { CARD, MOBILE_APP, CREDIT_CARD, DEVICE_MODEM, PARSE_ERROR }

    /**
     * Business deduplication key; a type-pattern switch over the sealed hierarchy so
     * there is exactly one key per kind.
     */
    default String key() {
        return switch (this) {
            case Card c -> c.rfid();
            case MobileApp app -> app.deviceId();
            case CreditCard cc -> cc.pan();
            case DeviceModem modem -> modem.imsi();
        };
    }

    /** Coarse classification into {@link Kind}; type-pattern switch, no need for a default. */
    default Kind kind() {
        return switch (this) {
            case Card ignored -> Kind.CARD;
            case MobileApp ignored -> Kind.MOBILE_APP;
            case CreditCard ignored -> Kind.CREDIT_CARD;
            case DeviceModem ignored -> Kind.DEVICE_MODEM;
        };
    }

    /** Human-readable description using record (deconstruction) patterns. */
    default String describe() {
        return switch (this) {
            case Card(String rfid, String number, String holder) ->
                    "card rfid=" + rfid + " number=" + number + " holder=" + holder;
            case MobileApp(String deviceId, String pushToken, Platform platform) ->
                    "mobile-app device=" + deviceId + " platform=" + platform;
            case CreditCard(String pan, String expiry) ->
                    "credit-card pan=" + pan + " expiry=" + expiry;
            case DeviceModem(String imsi, String iccid, String serial) ->
                    "device-modem imsi=" + imsi + " serial=" + serial;
        };
    }
}
