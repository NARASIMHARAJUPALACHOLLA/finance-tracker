package com.financetracker.dto;

import java.math.BigDecimal;

public class AccountStatsResponse {
    private int totalTransactions;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal savings;

    public AccountStatsResponse(int totalTransactions, BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal savings) {
        this.totalTransactions = totalTransactions;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.savings = savings;
    }

    public int getTotalTransactions() { return totalTransactions; }
    public BigDecimal getTotalIncome() { return totalIncome; }
    public BigDecimal getTotalExpense() { return totalExpense; }
    public BigDecimal getSavings() { return savings; }
}
