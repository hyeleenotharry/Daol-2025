package com.example.Daol_2025.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

@Configuration
public class GcpStorageConfig {

    @Value("${gcp.credentials}")
    private Resource credentialsResource;

    @Bean
    public Storage storage() throws IOException {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(credentialsResource.getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }
}
