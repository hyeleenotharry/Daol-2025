package com.example.Daol_2025.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import lombok.Value;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class InsuranceService {
    // 데이터베이스에 보험 정보 넣기
    private final Firestore firestore;

    public InsuranceService() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new ClassPathResource("daol-2025-firebase-adminsdk-fbsvc-dafc8a5b6b.json").getInputStream());
        FirestoreOptions options = FirestoreOptions.newBuilder()
                .setCredentials(credentials)
                .build();
        System.out.println("options 빌드 결과 : " + options);

        this.firestore = options.getService();  // issue
        System.out.println("Insurance service started");
    }
    // 사보험
    public String saveToFirestore(String collectionName, String documentId, Map<String, Object> data) throws ExecutionException, InterruptedException {
        WriteResult result = firestore.collection(collectionName)
                .document(documentId)
                .set(data)
                .get();
        return "Data saved at: " + result.getUpdateTime();
    }

    // 보험 모두 가져오기
    public List<Map<String, Object>> getAllInsurance() throws ExecutionException, InterruptedException{
        List<Map<String, Object>> insuranceList = new ArrayList<>();
        CollectionReference collectionRef = firestore.collection("insurance_products");
        ApiFuture<QuerySnapshot> future = collectionRef.get();

        QuerySnapshot querySnapshot = future.get();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {

            insuranceList.add(document.getData());  // Firestore 문서를 Map<String, Object> 형태로 저장
        }
        return insuranceList;
    }

    // 특정 보험 가져오기
    public Map<String, Object> getOneInsurance(String productId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("insurance_products").document(productId);
        ApiFuture<DocumentSnapshot> future = docRef.get();

        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.getData();
        } else{
            return null;
        }
    }
//    필수: 실손의료보험, 자동차보험(운전자라면)
//    추천: 암보험, 생명보험(가족이 있다면), 운전자보험, 배상책임보험
//    선택: 치아보험, 여행자보험, 연금보험 (개인 상황에 따라)


    // 본인이 입력한 정보에 따라 보험 정보 가져오기
}
