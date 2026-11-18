package org.example.dragon.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.example.dragon.exception.InvalidDataException;

import java.util.Set;
import java.util.stream.Collectors;

/** Проверяет тело запроса по Bean Validation и превращает нарушения в InvalidDataException (400). */
@ApplicationScoped
public class RequestValidator {

    @Inject
    private Validator validator;

    public <T> T validate(T body) {
        if (body == null) {
            throw new InvalidDataException("Request body is required");
        }
        Set<ConstraintViolation<T>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            throw new InvalidDataException(violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; ")));
        }
        return body;
    }
}
