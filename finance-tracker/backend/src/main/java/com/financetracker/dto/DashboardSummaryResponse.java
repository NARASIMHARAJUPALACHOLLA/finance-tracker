package com.financetracker.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummaryResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal savings;
    private BigDecimal budgetRemaining;
    private List<MonthlyTrendPoint> monthlyTrend;
    private List<CategoryAmount> categoryBreakdown;
    private List<TransactionResponse> recentTransactions;

    public DashboardSummaryResponse(FinanceSummary s, List<TransactionResponse> recentTransactions) {
        this.totalIncome = s.getTotalIncome();
        this.totalExpense = s.getTotalExpense();
        this.savings = s.getSavings();
        this.budgetRemaining = s.getBudgetRemaining();
        this.monthlyTrend = s.getMonthlyTrend();
        this.categoryBreakdown = s.getCategoryBreakdown();
        this.recentTransactions = recentTransactions;
    }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public BigDecimal getTotalExpense() { return totalExpense; }
    public BigDecimal getSavings() { return savings; }
    public BigDecimal getBudgetRemaining() { return budgetRemaining; }
    public List<MonthlyTrendPoint> getMonthlyTrend() { return monthlyTrend; }
    public List<CategoryAmount> getCategoryBreakdown() { return categoryBreakdown; }
    public List<TransactionResponse> getRecentTransactions() { return recentTransactions; }
}
