package com.systelm.dto;

public record ProfitSummaryRow(
    String period,
    double salesAmount,
    double costAmount,
    double profit
) {}
