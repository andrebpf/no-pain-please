package com.nopainplease.api.workout.adapter.in.web;

import com.nopainplease.api.workout.application.port.in.ListTrainingPlansUseCase;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/training-plans")
public class TrainingPlanController {
    private final ListTrainingPlansUseCase listTrainingPlans;

    public TrainingPlanController(ListTrainingPlansUseCase listTrainingPlans) {
        this.listTrainingPlans = listTrainingPlans;
    }

    @GetMapping
    public List<TrainingPlanResponse> list(HttpServletRequest request) {
        return listTrainingPlans.list(AuthenticatedUser.id(request)).stream()
                .map(TrainingPlanResponse::from)
                .toList();
    }
}
