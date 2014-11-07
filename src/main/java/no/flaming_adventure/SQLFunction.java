package no.flaming_adventure;

import java.sql.SQLException;

/**
 * Interface for function objects throwing SQL exceptions.
 *
 * @param <T>   Argument type.
 * @param <R>   Return type.
 */
public interface SQLFunction<T, R> {
    public R apply(T t) throws SQLException;
}
