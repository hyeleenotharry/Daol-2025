package com.example.Daol_2025.service;

import autovalue.shaded.com.google.common.base.Preconditions;
import com.google.cloud.storage.Storage;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class GcsPdfService {

    private final Storage storage;
    private final PdfParser pdfParser; // 앞서 만든 PDF 텍스트 추출기

    @Value("daol-2025-insurance")
    private String bucket = "daol-2025-insurance";

    public GcsPdfService(@Value("${gcp.credentials}") Resource credentialsFile) throws IOException {

        GoogleCredentials credentials = GoogleCredentials
                .fromStream(credentialsFile.getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        this.pdfParser = new PdfParser();
    }

    public String extractTextFromGcs(String bucket, String filePath) throws IOException {

        Preconditions.checkNotNull(bucket, "bucket is null!");
        Preconditions.checkNotNull(filePath, "filePath is null!");
        Blob blob = storage.get(bucket, filePath);
        if (blob == null) throw new FileNotFoundException("파일을 찾을 수 없습니다: " + filePath);

        try (ReadChannel reader = blob.reader();
             InputStream inputStream = Channels.newInputStream(reader)) {
            return pdfParser.extractTextFromPdf(inputStream);
        }
    }

    public void writeToGcs(String bucket, String outputPath, String content) {
        BlobId blobId = BlobId.of(bucket, outputPath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
        storage.create(blobInfo, content.getBytes(StandardCharsets.UTF_8));
    }

    public void uploadSummary(String docId, String content) {
        BlobId blobId = BlobId.of(bucket, "output/" + docId + "_summary.txt");
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
        storage.create(blobInfo, content.getBytes(StandardCharsets.UTF_8));
        System.out.println("📤 GCS 업로드 완료: output/" + docId + "_summary.txt");
    }
}

