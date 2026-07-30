package com.financetracker.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Deterministic analytics bundle produced by FinanceAnalyzerService.
 * Reused across the dashboard, reports, account stats, and as the grounding
 * data handed to the AI insight prompt.
 */
public class FinanceSummary {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal savings;
    private BigDecimal budgetRemaining;
    private List<CategoryAmount> categoryBreakdown;
    private List<MonthlyTrendPoint> monthlyTrend;
    private BigDecimal predictedExpense;
    private int confidence;
    private boolean budgetRisk;
    private List<String> insights;
    private List<String> recommendations;
    private int transactionCount;

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }
    public BigDecimal getSavings() { return savings; }
    public void setSavings(BigDecimal savings) { this.savings = savings; }
    public BigDecimal getBudgetRemaining() { return budgetRemaining; }
    public void setBudgetRemaining(BigDecimal budgetRemaining) { this.budgetRemaining = budgetRemaining; }
    public List<CategoryAmount> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(List<CategoryAmount> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }
    public List<MonthlyTrendPoint> getMonthlyTrend() { return monthlyTrend; }
    public void setMonthlyTrend(List<MonthlyTrendPoint> monthlyTrend) { this.monthlyTrend = monthlyTrend; }
    public BigDecimal getPredictedExpense() { return predictedExpense; }
    public void setPredictedExpense(BigDecimal predictedExpense) { this.predictedExpense = predictedExpense; }
    public int getConfidence() { return confidence; }
    public void setConfidence(int confidence) { this.confidence = confidence; }
    public boolean isBudgetRisk() { return budgetRisk; }
    public void setBudgetRisk(boolean budgetRisk) { this.budgetRisk = budgetRisk; }
    public List<String> getInsights() { return insights; }
    public void setInsights(List<String> insights) { this.insights = insights; }
    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
}
