package org.example.dragon.resource.error;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.example.dragon.exception.DatabaseFailureClassifier;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Всё остальное: проблемы БД -> 400/503/504, иначе 500. */
@Provider
public class FallbackExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(FallbackExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable error) {
        return DatabaseFailureClassifier.classify(error)
                .map(failure -> {
                    LOG.log(Level.WARNING, "Database failure: " + failure.message(), error);
                    return ErrorResponses.of(failure.status(), failure.message());
                })
                .orElseGet(() -> {
                    LOG.log(Level.SEVERE, "Unhandled error", error);
                    return ErrorResponses.of(500, "Internal server error");
                });
    }
}
