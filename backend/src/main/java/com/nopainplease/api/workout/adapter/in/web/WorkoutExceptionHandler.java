package com.nopainplease.api.workout.adapter.in.web;

import com.nopainplease.api.workout.application.exception.InvalidTrainingDayException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class WorkoutExceptionHandler {
    @ExceptionHandler(InvalidTrainingDayException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> invalidTrainingDay(InvalidTrainingDayException exception) {
        return Map.of("message", exception.getMessage());
    }
}
