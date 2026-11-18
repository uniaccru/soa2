package org.example.killer.exception;

/** Проблема при обращении к сервису 1; status — код, который вернём клиенту (502/503/504). */
public abstract class UpstreamException extends RuntimeException {

    private final int status;

    protected UpstreamException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
