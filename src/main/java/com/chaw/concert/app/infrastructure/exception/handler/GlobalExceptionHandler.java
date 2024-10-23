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
    public ResponseEntity<String> handleBaseException(BaseException ex) {
        HttpStatus status;
        switch (ex.getErrorType()) {
            case BAD_REQUEST:
                status = HttpStatus.BAD_REQUEST;
                break;
            case UNAUTHORIZED:
                status = HttpStatus.UNAUTHORIZED;
                break;
            case FORBIDDEN:
                status = HttpStatus.FORBIDDEN;
                break;
            case NOT_FOUND:
                status = HttpStatus.NOT_FOUND;
                break;
            case CONFLICT:
                status = HttpStatus.CONFLICT;
                break;
            default:
                logInternalServerErrors(ex);
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
        }

        return new ResponseEntity<>(ex.getMessage(), status);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        logInternalServerErrors(ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public void logInternalServerErrors(RuntimeException ex) {
        log.error("INTERNAL_SERVER_ERROR", ex);
        slackNotifierService.sendErrorNotificationToSlack(ex.getMessage());
    }
}
