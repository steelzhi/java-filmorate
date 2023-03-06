package ru.yandex.practicum.filmorate.exception;

public class NoSuitableUnitException extends RuntimeException{
    public NoSuitableUnitException(String message) {
        super(message);
    }

    public NoSuitableUnitException(String message, Throwable cause) {
        super(message, cause);
    }
}
