package com.nopainplease.api.workout.application.exception;

public class InvalidTrainingDayException extends RuntimeException {
    public InvalidTrainingDayException(String message) {
        super(message);
    }

    public InvalidTrainingDayException(String message, Throwable cause) {
        super(message, cause);
    }
}
