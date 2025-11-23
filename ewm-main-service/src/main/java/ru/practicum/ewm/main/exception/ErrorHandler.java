package ru.practicum.ewm.main.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(BadRequestException ex) {
        log.warn("Bad request (business): {}", ex.getMessage(), ex);
        return ApiError.of(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                ex.getMessage(),
                List.of(ex.toString())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var fieldError = ex.getBindingResult().getFieldError();
        String message;
        if (fieldError != null) {
            Object rejected = fieldError.getRejectedValue();
            message = String.format(
                    "Field: %s. Error: %s. Value: %s",
                    fieldError.getField(),
                    fieldError.getDefaultMessage(),
                    rejected
            );
        } else {
            message = "Validation failed";
        }
        log.warn("Validation error: {}", message, ex);
        return ApiError.of(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                message,
                List.of(ex.toString())
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolation(ConstraintViolationException ex) {
        var violation = ex.getConstraintViolations().stream().findFirst().orElse(null);
        String message;
        if (violation != null) {
            message = String.format(
                    "Field: %s. Error: %s. Value: %s",
                    violation.getPropertyPath(),
                    violation.getMessage(),
                    violation.getInvalidValue()
            );
        } else {
            message = ex.getMessage();
        }
        log.warn("Constraint violation: {}", message, ex);
        return ApiError.of(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                message,
                List.of(ex.toString())
        );
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(RuntimeException ex) {
        log.warn("Bad request: {}", ex.getMessage(), ex);
        return ApiError.of(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                ex.getMessage(),
                List.of(ex.toString())
        );
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(RuntimeException ex) {
        log.warn("Not found: {}", ex.getMessage(), ex);
        return ApiError.of(
                HttpStatus.NOT_FOUND,
                "The required object was not found.",
                ex.getMessage(),
                List.of(ex.toString())
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();
        log.warn("Data integrity violation: {}", message, ex);
        return ApiError.of(
                HttpStatus.CONFLICT,
                "Integrity constraint has been violated.",
                message,
                List.of(ex.toString())
        );
    }

    @ExceptionHandler(ConditionsNotMetException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConditionsNotMet(RuntimeException ex) {
        log.warn("Conflict (conditions not met): {}", ex.getMessage(), ex);
        return ApiError.of(
                HttpStatus.CONFLICT,
                "For the requested operation the conditions are not met.",
                ex.getMessage(),
                List.of(ex.toString())
        );
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleAll(Throwable ex) {
        log.error("Unexpected error", ex);
        return ApiError.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected error.",
                ex.getMessage(),
                List.of(ex.toString())
        );
    }
}


