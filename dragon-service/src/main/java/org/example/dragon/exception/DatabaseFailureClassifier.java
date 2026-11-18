package org.example.dragon.exception;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Optional;

/**
 * Определяет по цепочке причин, что исключение вызвано проблемой БД:
 * нарушение целостности -> 400, таймаут -> 504, нет соединения -> 503.
 * Классы Hibernate и драйвера проверяются по имени, чтобы не зависеть от них при компиляции.
 */
public final class DatabaseFailureClassifier {

    public record Failure(int status, String message) {
    }

    private static final int MAX_DEPTH = 20;

    private DatabaseFailureClassifier() {
    }

    public static Optional<Failure> classify(Throwable error) {
        boolean timeout = false;
        boolean unavailable = false;
        Throwable current = error;
        for (int depth = 0; current != null && depth < MAX_DEPTH; depth++) {
            if (current instanceof SQLException sql) {
                String state = sql.getSQLState();
                if (state != null && state.startsWith("23")) {
                    return Optional.of(new Failure(400, "Data violates integrity constraints"));
                }
                if (state != null && state.startsWith("08")) {
                    unavailable = true;
                }
            }
            String className = current.getClass().getName();
            if (current instanceof SQLTimeoutException
                    || className.contains("Timeout")
                    || className.contains("TimedOut")) {
                timeout = true;
            }
            if (className.contains("JDBCConnectionException")
                    || className.contains("SQLTransientConnectionException")
                    || className.contains("ConnectException")
                    || className.startsWith("jakarta.resource.")) {
                unavailable = true;
            }
            String message = current.getMessage();
            if (message != null && message.contains("WFLYJCA")) {
                unavailable = true;
            }
            current = current.getCause() == current ? null : current.getCause();
        }
        if (timeout) {
            return Optional.of(new Failure(504, "Database did not respond in time"));
        }
        if (unavailable) {
            return Optional.of(new Failure(503, "Database is unavailable"));
        }
        return Optional.empty();
    }
}
