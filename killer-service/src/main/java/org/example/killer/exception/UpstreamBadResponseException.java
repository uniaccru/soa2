package org.example.killer.exception;

/** Сервис 1 вернул некорректный или неожиданный ответ (502). */
public class UpstreamBadResponseException extends UpstreamException {

    public UpstreamBadResponseException(String message) {
        super(502, message);
    }
}
