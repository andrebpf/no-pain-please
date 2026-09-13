package com.nopainplease.api.workout.adapter.in.web;

import com.nopainplease.api.workout.application.port.in.CreateWorkoutUseCase;
import com.nopainplease.api.workout.application.port.in.ListWorkoutsUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {
    private final CreateWorkoutUseCase createWorkout;
    private final ListWorkoutsUseCase listWorkouts;

    public WorkoutController(
            CreateWorkoutUseCase createWorkout,
            ListWorkoutsUseCase listWorkouts) {
        this.createWorkout = createWorkout;
        this.listWorkouts = listWorkouts;
    }

    @GetMapping
    public List<WorkoutResponse> list(HttpServletRequest request) {
        return listWorkouts.listRecent(AuthenticatedUser.id(request)).stream()
                .map(WorkoutResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutResponse create(
            HttpServletRequest request,
            @Valid @RequestBody CreateWorkoutRequest body) {
        return WorkoutResponse.from(
                createWorkout.create(AuthenticatedUser.id(request), body.toCommand()));
    }
}
