package com.example.Daol_2025.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseDatabase firebaseDatabase() throws IOException {
        // Load the Firebase service account key JSON file
        ClassPathResource resource = new ClassPathResource("daol-2025-firebase-adminsdk-fbsvc-dafc8a5b6b.json");

        if (!resource.exists()) {
            throw new FileNotFoundException("Firebase config file not found in classpath: " + resource.getPath());
        }

        InputStream serviceAccount = resource.getInputStream();

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

