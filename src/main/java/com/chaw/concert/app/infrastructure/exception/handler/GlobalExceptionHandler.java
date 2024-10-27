package com.chaw.concert.app.infrastructure.exception.handler;

import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.slack.SlackNotifierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final SlackNotifierService slackNotifierService;

    public GlobalExceptionHandler(SlackNotifierService slackNotifierService) {
        this.slackNotifierService = slackNotifierService;
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ExceptionResponse> handleBaseException(BaseException ex) {
        HttpStatus status = ex.getErrorType().getHttpStatus();
        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            logInternalServerErrors(ex);
        }

        return new ResponseEntity<>(new ExceptionResponse(ex.getMessage(), status), status);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionResponse> handleRuntimeException(RuntimeException ex) {
        logInternalServerErrors(ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(new ExceptionResponse(ex.getMessage(), status), status);
    }

    public void logInternalServerErrors(RuntimeException ex) {
        log.error(HttpStatus.INTERNAL_SERVER_ERROR.name(), ex);
        slackNotifierService.sendErrorNotificationToSlack(ex.getMessage());
    }

    public record ExceptionResponse(String message, String status, int code) {
        public ExceptionResponse(String message, HttpStatus status) {
            this(message, status.getReasonPhrase(), status.value());
        }
    }
}
