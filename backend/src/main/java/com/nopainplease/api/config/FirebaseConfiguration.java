package com.nopainplease.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.NoCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfiguration {
  @Value("${app.firebase.project-id:}") private String projectId;
  @Value("${app.firebase.firestore-emulator-host:}") private String firestoreEmulatorHost;
  @Bean
  FirebaseApp firebaseApp() throws IOException {
    if (!FirebaseApp.getApps().isEmpty()) return FirebaseApp.getInstance();
    FirebaseOptions.Builder options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.getApplicationDefault());
    if (!projectId.isBlank()) options.setProjectId(projectId);
    return FirebaseApp.initializeApp(options.build());
  }

  @Bean
  Firestore firestore(FirebaseApp app) {
    if (!firestoreEmulatorHost.isBlank()) {
      return FirestoreOptions.newBuilder()
          .setProjectId(projectId)
          .setEmulatorHost(firestoreEmulatorHost)
          .setCredentials(NoCredentials.getInstance())
          .build()
          .getService();
    }
    return FirestoreClient.getFirestore(app);
  }
}
