package com.example.Daol_2025.domain;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
public class Policy {
    private String serviceId;           // 서비스 ID
    private String serviceName;         // 서비스명
    private String serviceSummary;      // 서비스 목적 요약
    private String serviceField;        // 서비스 분야
    private String eligibilityCriteria; // 선정 기준
    private String issuingAgency;       // 소관 기관명
    private String agencyType;          // 소관 기관 유형
    private String agencyCode;          // 소관 기관 코드
    private String applicationDeadline; // 신청 기한
    private String applicationMethod;   // 신청 방법
    private String contactNumbers;      // 전화 문의 (여러 개일 수 있음)
    private String applicationAgency;   // 접수 기관
    private int viewCount;              // 조회수
    private String supportDetails;      // 지원 내용
    private String supportTarget;       // 지원 대상
    private List<String> supportTypes;  // 지원 유형 (다중 값)

    private String detailUrl;           // 상세 조회 URL
    private String lastUpdated;         // 수정일시
    private String department;          // 부서명
    private String userType;            // 사용자 구분
    private String registrationDate;    // 등록일시
}
