package com.financetracker.dto;

import java.math.BigDecimal;

public class PredictionResponse {
    private BigDecimal predictedExpense;
    private int confidence;
    private boolean budgetRisk;

    public PredictionResponse(BigDecimal predictedExpense, int confidence, boolean budgetRisk) {
        this.predictedExpense = predictedExpense;
        this.confidence = confidence;
        this.budgetRisk = budgetRisk;
    }

    public BigDecimal getPredictedExpense() { return predictedExpense; }
    public int getConfidence() { return confidence; }
    public boolean isBudgetRisk() { return budgetRisk; }
}
