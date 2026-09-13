package com.nopainplease.api.workout.adapter.out.firestore;

import com.google.api.core.ApiFuture;
import java.util.concurrent.ExecutionException;

final class FirestoreOperation {
    private FirestoreOperation() {
    }

    static <T> T await(ApiFuture<T> future, String failureMessage) {
        try {
            return future.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(failureMessage, exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException(failureMessage, exception.getCause());
        }
    }
}
