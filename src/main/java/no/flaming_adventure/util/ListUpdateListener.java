package no.flaming_adventure.util;

import javafx.collections.ListChangeListener;

import java.util.function.Consumer;

/**
 * A {@link javafx.collections.ListChangeListener list change listener} that calls a
 * given function for every updated item every time the list is updated.
 *
 * @param <T> the type of the list elements.
 */
public class ListUpdateListener<T> implements ListChangeListener<T> {

    /***************************************************************************
     *                                                                         *
     * Instance Variables                                                      *
     *                                                                         *
     **************************************************************************/

    private final Consumer<T> updateHook;

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Construct a list update listener with the given update function.
     *
     * @param updateHook a function taking a single object of type {@link T}. This
     *                   function will be called for every updated item every time
     *                   the list is updated.
     */
    public ListUpdateListener(Consumer<T> updateHook) {
        this.updateHook = updateHook;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * Function to be called whenever the list is modified in any way.
     *
     * @param change a set of changes.
     */
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
