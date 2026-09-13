package com.nopainplease.api.workout.adapter.in.web;

import com.nopainplease.api.workout.application.port.in.ListExerciseWeightHistoryUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/exercise-weight-history")
public class ExerciseWeightHistoryController {
    private final ListExerciseWeightHistoryUseCase listHistory;

    public ExerciseWeightHistoryController(ListExerciseWeightHistoryUseCase listHistory) {
        this.listHistory = listHistory;
    }

    @GetMapping("/{exerciseId}")
    public List<ExerciseWeightHistoryResponse> list(
            HttpServletRequest request,
            @PathVariable @Pattern(regexp = "[A-Za-z0-9_-]{1,80}") String exerciseId) {
        return listHistory.list(AuthenticatedUser.id(request), exerciseId).stream()
                .map(point -> new ExerciseWeightHistoryResponse(
                        point.date().toString(), point.weightKg()))
                .toList();
    }

    public record ExerciseWeightHistoryResponse(String date, double weightKg) {
    }
}
