package com.example.Daol_2025.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseDatabase firebaseDatabase() throws IOException {
        // Load the Firebase service account key JSON file
        FileInputStream serviceAccount = new FileInputStream("src/main/resources/daol-2025-firebase-adminsdk-fbsvc-dafc8a5b6b.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://daol-2025-default-rtdb.firebaseio.com/") // Change to your actual Firebase URL
                .build();

        // Initialize FirebaseApp if not already initialized
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        // Return the FirebaseDatabase instance
        return FirebaseDatabase.getInstance(FirebaseApp.getInstance());
    }
}

