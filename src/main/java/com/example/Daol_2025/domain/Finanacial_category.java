package com.example.Daol_2025.domain;

public enum Finanacial_category {
    Demand_deposit,
    Fixed_deposit,
    Recurring_deposit,
    Special_deposit_products
}

// 요구불 예금 (Demand Deposit)
// 자유롭게 입출금할 수 있는 예금으로, 이자가 낮거나 없는 경우가 많음
//    보통예금 (Regular Savings Account) : 가장 기본적인 입출금 가능 계좌
//    당좌예금 (Checking Account) : 수표 발행이 가능한 계좌 (한국에서는 개인보다는 기업 중심)
//    저축예금 (Passbook Savings Account) : 일정 금액 이상 유지 시 약간의 이자가 붙는 자유입출금 계좌
//    MMDA (Money Market Deposit Account) : 요구불 예금이지만, 일반 보통예금보다 높은 금리를 제공

// 정기예금 (Time Deposit / Fixed Deposit, FD)
// 일정 기간 동안 돈을 맡기고, 약정한 기간이 지나야 인출할 수 있는 예금
//    정기예금 (Fixed Deposit, FD / Time Deposit) : 일정 기간 돈을 묶어두고, 만기 후 원금과 이자를 받는 예금
//    양도성예금증서 (Certificate of Deposit, CD) : 양도가 가능한 정기예금증서 (한국에서는 기관투자가 중심)

// 정기적금 (Installment Savings / Recurring Deposit, RD)
// 매월 일정 금액을 납입하고, 만기에 원금과 이자를 받는 적금
//    정기적금 (Recurring Deposit, RD / Installment Savings) : 매월 일정 금액을 불입하고, 만기 후 수령
//    자유적금 (Flexible Recurring Deposit) : 납입금액을 자유롭게 조절할 수 있는 적금

// 특수 예적금 상품 (Special Deposit Products)
// 특정한 목적이나 혜택이 포함된 예적금
//    ISA (Individual Savings Account, 개인종합자산관리계좌) : 다양한 금융상품을 담을 수 있는 절세형 저축계좌
//    청년/고령자 전용 적금 (Youth/Senior Savings Plan) : 특정 연령층을 위한 정부 지원 적금
//    외화예금 (Foreign Currency Deposit, FX Deposit) : 외화를 직접 예치하는 예금
//    연금저축 (Pension Savings Account) : 노후 대비를 위한 장기 저축 계좌