package com.nopainplease.api.workout.adapter.config;

import com.nopainplease.api.workout.application.port.out.TrainingDayRepository;
import com.nopainplease.api.workout.application.port.out.TrainingPlanRepository;
import com.nopainplease.api.workout.application.port.out.WorkoutRepository;
import com.nopainplease.api.workout.application.service.TrainingDayService;
import com.nopainplease.api.workout.application.service.TrainingPlanService;
import com.nopainplease.api.workout.application.service.WorkoutService;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkoutConfiguration {
    @Bean
    Clock applicationClock() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }

    @Bean
    WorkoutService workoutService(WorkoutRepository repository, Clock clock) {
        return new WorkoutService(repository, clock);
    }

    @Bean
    TrainingPlanService trainingPlanService(TrainingPlanRepository repository) {
        return new TrainingPlanService(repository);
    }

    @Bean
    TrainingDayService trainingDayService(
            TrainingDayRepository days,
            TrainingPlanRepository plans,
            Clock clock) {
        return new TrainingDayService(days, plans, clock);
    }

}
