package org.example.killer.exception;

/** Сервис 1 не ответил вовремя (504). */
public class UpstreamTimeoutException extends UpstreamException {

    public UpstreamTimeoutException(String message) {
        super(504, message);
    }
}
