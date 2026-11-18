package org.example.killer.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.example.killer.dto.ErrorDto;
import org.example.killer.exception.InvalidDataException;
import org.example.killer.exception.ResourceNotFoundException;
import org.example.killer.exception.UpstreamException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Единственное место, где исключения превращаются в ответ вида <error><code/><message/></error>.
 * Ошибки самого Spring (404 на неизвестный путь, 405, 415, ...) тоже идут через handleExceptionInternal.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Log LOG = LogFactory.getLog(ApiExceptionHandler.class);

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<Object> invalidData(InvalidDataException e) {
        return error(400, e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> notFound(ResourceNotFoundException e) {
        return error(404, e.getMessage());
    }

    @ExceptionHandler(UpstreamException.class)
    public ResponseEntity<Object> upstream(UpstreamException e) {
        return error(e.status(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> unexpected(Exception e) {
        LOG.error("Unhandled error", e);
        return error(500, "Internal server error");
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        return error(statusCode.value(), ex.getMessage());
    }

    private ResponseEntity<Object> error(int status, String message) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_XML)
                .body(new ErrorDto(status, message));
    }
}
