package com.nopainplease.api.workout;

import jakarta.validation.Validation;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class TrainingDayTest {
  private final String today = LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString();
  private TrainingDay day(String date, Double weight, boolean completed) {
    return new TrainingDay(date, "A", completed, List.of(new TrainingDay.ExerciseProgress("A-1", weight, completed)));
  }

  @Test void permitsUnrecordedAndZeroWeights() {
    try (var factory = Validation.buildDefaultValidatorFactory()) {
      var validator = factory.getValidator();
      assertTrue(validator.validate(day(today, null, true)).isEmpty());
      assertTrue(validator.validate(day(today, 0.0, false)).isEmpty());
    }
  }

  @Test void rejectsNegativeAndExcessiveWeights() {
    try (var factory = Validation.buildDefaultValidatorFactory()) {
      var validator = factory.getValidator();
      assertFalse(validator.validate(day(today, -0.5, false)).isEmpty());
      assertFalse(validator.validate(day(today, 2000.5, false)).isEmpty());
    }
  }

  @Test void rejectsInvalidFutureAndMismatchingDates() {
    assertThrows(ResponseStatusException.class, () -> TrainingDayController.validateDay("2026-02-30", day("2026-02-30", 10.0, false)));
    assertThrows(ResponseStatusException.class, () -> TrainingDayController.validateDay("2999-01-01", day("2999-01-01", 10.0, false)));
    assertThrows(ResponseStatusException.class, () -> TrainingDayController.validateDay("2020-01-01", day(today, 10.0, false)));
    assertDoesNotThrow(() -> TrainingDayController.validateDay("2020-01-01", day("2020-01-01", 10.0, true)));
  }

  @Test void rejectsDuplicateExercisesAndNonFiniteWeights() {
    var entry = new TrainingDay.ExerciseProgress("A-1", 10.0, true);
    assertThrows(ResponseStatusException.class, () -> TrainingDayController.validateDay(today, new TrainingDay(today, "A", true, List.of(entry, entry))));
    assertThrows(ResponseStatusException.class, () -> TrainingDayController.validateDay(today, day(today, Double.NaN, false)));
  }
}
