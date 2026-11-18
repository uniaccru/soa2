package org.example.dragon.exception;

/** Данные нарушают ограничения (HTTP 400). */
public class InvalidDataException extends RuntimeException {

    public InvalidDataException(String message) {
        super(message);
    }
}
