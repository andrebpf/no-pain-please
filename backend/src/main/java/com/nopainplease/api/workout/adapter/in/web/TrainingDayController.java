package com.nopainplease.api.workout.adapter.in.web;

import com.nopainplease.api.workout.application.exception.InvalidTrainingDayException;
import com.nopainplease.api.workout.application.port.in.ListTrainingDaysUseCase;
import com.nopainplease.api.workout.application.port.in.SaveTrainingDayUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/training-days")
public class TrainingDayController {
    private final ListTrainingDaysUseCase listTrainingDays;
    private final SaveTrainingDayUseCase saveTrainingDay;

    public TrainingDayController(
            ListTrainingDaysUseCase listTrainingDays,
            SaveTrainingDayUseCase saveTrainingDay) {
        this.listTrainingDays = listTrainingDays;
        this.saveTrainingDay = saveTrainingDay;
    }

    @GetMapping
    public List<TrainingDayResponse> list(HttpServletRequest request) {
        return listTrainingDays.list(AuthenticatedUser.id(request)).stream()
                .map(TrainingDayResponse::from)
                .toList();
    }

    @PutMapping("/{date}")
    public TrainingDayResponse save(
            HttpServletRequest request,
            @PathVariable String date,
            @Valid @RequestBody TrainingDayRequest body) {
        return TrainingDayResponse.from(saveTrainingDay.save(
                AuthenticatedUser.id(request), parseDate(date), body.toDomain()));
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeException exception) {
            throw new InvalidTrainingDayException("Invalid training date", exception);
        }
    }
}
