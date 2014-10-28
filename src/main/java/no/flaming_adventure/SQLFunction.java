package no.flaming_adventure;

import java.sql.SQLException;

public interface SQLFunction<T, R> {
    public R apply(T t) throws SQLException;
}
