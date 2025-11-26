package ru.practicum.ewm.main.exception;


public class ConditionsNotMetException extends ConflictException {
    public ConditionsNotMetException(String message) {
        super(message);
    }
}