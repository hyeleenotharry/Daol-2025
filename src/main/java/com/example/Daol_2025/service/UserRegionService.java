package com.example.Daol_2025.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class UserRegionService {

    public String getAddressFromPython() {
        try {
            // Python 스크립트 경로
            String scriptPath = "/Users/kyuseung/Desktop/Daol-python/app.py";

            // Python 프로세스 실행
            ProcessBuilder pb = new ProcessBuilder("python3", scriptPath);
            Process process = pb.start();

            // Python 프로세스의 출력 읽기
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            // 프로세스가 종료될 때까지 대기
            process.waitFor();
            return output.toString(); // JSON 결과 반환
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Python 스크립트 실행 실패\"}";
        }
    }
}
