package dev.bottega.streams.almostlikefunctional.excercise;

class Parser {

    static TokenImport.Row row(String rawLine) {
        return new TokenImport.Row(rawLine, rawLine.split("\\s*,\\s*"));
    }

    /**
     * Business dispatch over every token kind (inline switch). Each per-kind business rule
     * lives inside its own {@code parse*} function, so a {@link Token} is only ever built
     * when it passes validation.
     */
    static Result<Token> parseToken(TokenImport.Row row) {
        return switch (row.kind()) {
            case "CARD" -> parseCard(row.columns(), row.raw());
            case "MOBILE_APP" -> parseMobileApp(row.columns(), row.raw());
            case "CREDIT_CARD" -> parseCreditCard(row.columns(), row.raw());
            case "DEVICE_MODEM" -> parseDeviceModem(row.columns(), row.raw());
            default -> Result.err(new TokenImport.Problem.UnknownKind(row.kind()));
        };
    }

    // ===== per-type parsing details (parse + validate in one step) =====

    private static Result<Token> parseCard(String[] c, String line) {
        if (c.length < 4) {
            return Result.err(new TokenImport.Problem.InvalidFormat(line, "CARD requires rfid, visual number and holder"));
        }
        String rfid = c[1], visual = c[2], holder = c[3];
        if (rfid.isBlank() || visual.isBlank()) {
            return Result.err(new TokenImport.Problem.BusinessRule("card requires a non-blank rfid and visual number"));
        }
        return Result.ok(new Token.Card(rfid, visual, holder));
    }

    private static Result<Token> parseMobileApp(String[] c, String line) {
        if (c.length < 4) {
            return Result.err(new TokenImport.Problem.InvalidFormat(line, "MOBILE_APP requires deviceId, pushToken, platform"));
        }
        String deviceId = c[1], pushToken = c[2];
        if (pushToken.isBlank()) {
            return Result.err(new TokenImport.Problem.BusinessRule("mobile-app requires a non-blank push token"));
        }
        return parsePlatform(c[3], line).map(platform -> new Token.MobileApp(deviceId, pushToken, platform));
    }

    private static Result<Token> parseCreditCard(String[] c, String line) {
        if (c.length < 3) {
            return Result.err(new TokenImport.Problem.InvalidFormat(line, "CREDIT_CARD requires pan and expiry"));
        }
        String pan = c[1], expiry = c[2];
        if (!pan.matches("\\d{13,19}") || expiry.isBlank()) {
            return Result.err(new TokenImport.Problem.BusinessRule("credit-card pan must be 13-19 digits and expiry non-blank"));
        }
        return Result.ok(new Token.CreditCard(pan, expiry));
    }

    private static Result<Token> parseDeviceModem(String[] c, String line) {
        if (c.length < 4) {
            return Result.err(new TokenImport.Problem.InvalidFormat(line, "DEVICE_MODEM requires imsi, iccid, serial"));
        }
        String imsi = c[1];
        if (imsi.isBlank()) {
            return Result.err(new TokenImport.Problem.BusinessRule("device-modem requires a non-blank imsi"));
        }
        return Result.ok(new Token.DeviceModem(imsi, c[2], c[3]));
    }

    private static Result<Token.Platform> parsePlatform(String raw, String line) {
        return switch (raw.strip().toUpperCase()) {
            case "ANDROID" -> Result.ok(Token.Platform.ANDROID);
            case "IOS" -> Result.ok(Token.Platform.IOS);
            default -> Result.err(new TokenImport.Problem.InvalidFormat(line, "unknown platform: " + raw));
        };
    }
}
