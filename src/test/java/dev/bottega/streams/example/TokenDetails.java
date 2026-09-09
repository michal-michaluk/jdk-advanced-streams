package dev.bottega.streams.example;

import lombok.Builder;

public sealed interface TokenDetails {

    static Card card(String rfid, String number) {
        return new Card(rfid, number, null, null);
    }

    static Card card(String rfid, String number, String name) {
        return new Card(rfid, number, name, null);
    }

    record NoDetails() implements TokenDetails {
    }

    @Builder
    record Card(
            String rfid,
            String number,
            String name,
            Boolean onlineAuthorizationRequired) implements TokenDetails {
    }
}
