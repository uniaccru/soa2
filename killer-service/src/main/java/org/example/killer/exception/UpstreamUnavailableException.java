package org.example.killer.exception;

/** Сервис 1 недоступен (503). */
public class UpstreamUnavailableException extends UpstreamException {

    public UpstreamUnavailableException(String message) {
        super(503, message);
    }
}
