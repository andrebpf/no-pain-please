package com.nopainplease.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfiguration {
  @Bean
  FirebaseApp firebaseApp() throws IOException {
    if (!FirebaseApp.getApps().isEmpty()) return FirebaseApp.getInstance();
    FirebaseOptions options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.getApplicationDefault())
        .build();
    return FirebaseApp.initializeApp(options);
  }

  @Bean
  Firestore firestore(FirebaseApp app) { return FirestoreClient.getFirestore(app); }
}
