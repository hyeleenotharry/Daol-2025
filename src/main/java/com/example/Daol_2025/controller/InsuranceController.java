package com.example.Daol_2025.controller;
import com.example.Daol_2025.service.InsuranceService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/insurance")
public class InsuranceController {

    @Autowired
    private InsuranceService insuranceService;

    @PostMapping("/save")
    public String saveData(@RequestParam String filepath) throws IOException, ExecutionException, InterruptedException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> jsonData = objectMapper.readValue(new File(filepath), List.class);
            for (Map<String, Object> item : jsonData) {
                String productId = (String) item.get("productId");
                insuranceService.saveToFirestore("insurance_products", productId, item);
            }
            return "success";
        } catch (Exception e) {
            return e.getMessage();
        }

    }

    @GetMapping("/")
    public Object allInsurances(@RequestParam(value = "productId", required = false) String productId) throws ExecutionException, InterruptedException {
        if (Objects.isNull(productId)) {
            return insuranceService.getAllInsurance();
        } else {
            Map<String, Object> insurance = insuranceService.getOneInsurance(productId);
            return (insurance != null) ? insurance : "해당 보험을 찾을 수 없습니다.";
        }
    }


    // 보험 가져오기

}
