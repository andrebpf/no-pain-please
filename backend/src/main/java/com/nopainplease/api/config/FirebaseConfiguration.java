package com.nopainplease.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.AccessToken;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

@Configuration
public class FirebaseConfiguration {
  @Value("${app.firebase.project-id:}") private String projectId;
  @Value("${app.firebase.firestore-emulator-host:}") private String firestoreEmulatorHost;
  @Bean
  FirebaseApp firebaseApp(Environment environment) throws IOException {
    if (!FirebaseApp.getApps().isEmpty()) return FirebaseApp.getInstance();
    boolean local = environment.acceptsProfiles(Profiles.of("local"));
    String authEmulatorHost = System.getenv("FIREBASE_AUTH_EMULATOR_HOST");
    if (local && (authEmulatorHost == null || authEmulatorHost.isBlank()
        || firestoreEmulatorHost.isBlank() || projectId.isBlank())) {
      throw new IllegalStateException(
          "The local profile requires a project ID and Auth and Firestore emulator hosts.");
    }
    // Only local emulator runs use a placeholder token. Cloud Run uses its service identity.
    GoogleCredentials credentials = local
        ? GoogleCredentials.create(new AccessToken("local-emulator-only", null))
        : GoogleCredentials.getApplicationDefault();
    FirebaseOptions.Builder options = FirebaseOptions.builder()
        .setCredentials(credentials);
    if (!projectId.isBlank()) options.setProjectId(projectId);
    return FirebaseApp.initializeApp(options.build());
  }

  @Bean
  Firestore firestore(FirebaseApp app) {
    if (!firestoreEmulatorHost.isBlank()) {
      return FirestoreOptions.newBuilder()
          .setProjectId(projectId)
          .setEmulatorHost(firestoreEmulatorHost)
          // NoCredentials selects a legacy transport that ignores emulator settings.
          .setCredentials(GoogleCredentials.create(new AccessToken("owner", null)))
          .build()
          .getService();
    }
    return FirestoreClient.getFirestore(app);
  }
}
