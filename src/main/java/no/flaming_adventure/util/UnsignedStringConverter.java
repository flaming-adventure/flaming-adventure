package no.flaming_adventure.util;

import javafx.util.StringConverter;

public class UnsignedStringConverter extends StringConverter<Number> {
    @Override public String toString(Number object) {
        return object.toString();
    }

    @Override public Number fromString(String string) {
        return Integer.parseUnsignedInt(string);
    }
}
