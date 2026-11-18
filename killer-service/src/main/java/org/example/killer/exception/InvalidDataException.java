package org.example.killer.exception;

/** Невалидные данные запроса (HTTP 400). */
public class InvalidDataException extends RuntimeException {

    public InvalidDataException(String message) {
        super(message);
    }
}
