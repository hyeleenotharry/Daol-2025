package com.example.Daol_2025.controller;

import com.example.Daol_2025.service.DocumentQueueService;
import com.example.Daol_2025.service.GcsPdfService;
import com.example.Daol_2025.service.ResultMerger;

import com.google.api.client.util.Value;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


@RestController
@RequestMapping("/gcs")
public class GcsSummaryController {

    private final GcsPdfService gcsPdfService;
    private final DocumentQueueService queueService;
    private final ResultMerger resultMerger;

    @Value("daol-2025-insurance")
    private String bucket = "daol-2025-insurance";

    @Autowired
    private Storage storage;

    public GcsSummaryController(GcsPdfService gcsPdfService,
                                DocumentQueueService queueService,
                                ResultMerger resultMerger) {
        this.gcsPdfService = gcsPdfService;
        this.queueService = queueService;
        this.resultMerger = resultMerger;
    }

    @PostMapping("/summarize")
    public String summarizePdfFromGcs(@RequestParam String fileName) throws IOException {

        String inputPath = "input/" + fileName;
        String docId = fileName.replace(".pdf", "");

        // 1. GCS에서 PDF 텍스트 추출
        String content = gcsPdfService.extractTextFromGcs(bucket, inputPath);

        // 2. Redis 큐에 분할 저장
        queueService.enqueue(docId, content);

        return "✅ 처리 시작됨: " + docId + " (분할 완료)";
    }

    @PostMapping("/finalize")
    public String mergeAndStoreResult(@RequestParam String fileName) {
        String docId = fileName.replace(".pdf", "");
        String result = resultMerger.merge(docId);

        if (result == null) return "❌ 아직 요약 결과 없음";

        // 3. GCS output/에 결과 저장
        String outputPath = "output/" + docId + "_summary.txt";
        gcsPdfService.writeToGcs(bucket, outputPath, result);

        return "✅ 요약 저장 완료: gs://" + bucket + "/" + outputPath;
    }

    @PostMapping("/summary-content")
    public String readSummaryContent(@RequestParam String fileName) {
        String docId = fileName.replace(".pdf", "");
        String objectName = "output/" + docId + "_summary.txt";
        System.out.println(objectName);
        Blob blob = storage.get(bucket, objectName);
        if (blob == null || !blob.exists()) {
            return "❌ 파일을 찾을 수 없습니다: " + objectName;
        }

        byte[] contentBytes = blob.getContent();
        return new String(contentBytes, StandardCharsets.UTF_8);
    }
}

