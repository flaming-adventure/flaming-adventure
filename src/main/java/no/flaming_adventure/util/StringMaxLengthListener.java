package no.flaming_adventure.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.function.Consumer;

public class StringMaxLengthListener implements ChangeListener<String> {
    private final Integer maxLength;
    private final Consumer<String> consumer;

    public StringMaxLengthListener(Integer maxLength, Consumer<String> consumer) {
        this.maxLength = maxLength;
        this.consumer  = consumer;
    }

    @Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (newValue.length() > maxLength) {
            consumer.accept(newValue.substring(0, maxLength));
        }
    }
}
