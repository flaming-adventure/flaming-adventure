package no.flaming_adventure.util;

import javafx.collections.ListChangeListener;

import java.util.function.Consumer;

public class ListUpdateListener<T> implements ListChangeListener<T> {
    private final Consumer<T> updateHook;

    public ListUpdateListener(Consumer<T> updateHook) {
        this.updateHook = updateHook;
    }

    @Override public void onChanged(Change<? extends T> change) {
        while (change.next()) {
            if (change.wasUpdated()) {
                change.getList()
                      .subList(change.getFrom(), change.getTo())
                      .forEach(updateHook);
            }
        }
    }
}
