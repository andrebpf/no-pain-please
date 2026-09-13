package com.nopainplease.api.workout.domain.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrainingDayTest {
    @Test
    void permitsUnrecordedAndZeroWeights() {
        var unrecorded = new ExerciseProgress("A-1", null, false);
        var zeroWeight = new ExerciseProgress("A-2", 0.0, false);

        var day = new TrainingDay(
                LocalDate.of(2026, 9, 13), "A", false, List.of(unrecorded, zeroWeight));

        assertFalse(day.attended());
    }

    @Test
    void rejectsInvalidWeights() {
        assertThrows(IllegalArgumentException.class,
                () -> new ExerciseProgress("A-1", -0.5, false));
        assertThrows(IllegalArgumentException.class,
                () -> new ExerciseProgress("A-1", 2000.5, false));
        assertThrows(IllegalArgumentException.class,
                () -> new ExerciseProgress("A-1", Double.NaN, false));
    }

    @Test
    void rejectsDuplicateExercises() {
        var entry = new ExerciseProgress("A-1", 10.0, true);

        assertThrows(IllegalArgumentException.class, () -> new TrainingDay(
                LocalDate.of(2026, 9, 13), "A", true, List.of(entry, entry)));
    }

    @Test
    void completedExerciseRecordsAttendance() {
        var day = new TrainingDay(
                LocalDate.of(2026, 9, 13),
                "A",
                false,
                List.of(new ExerciseProgress("A-1", null, true)));

        assertTrue(day.attended());
    }
}
