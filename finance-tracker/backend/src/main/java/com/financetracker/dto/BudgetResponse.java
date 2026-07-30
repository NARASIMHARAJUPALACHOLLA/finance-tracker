package com.financetracker.dto;

import java.math.BigDecimal;
import java.util.List;

public class BudgetResponse {
    private BigDecimal monthlyBudget;
    private List<CategoryBudgetDto> categoryBudgets;

    public BudgetResponse(BigDecimal monthlyBudget, List<CategoryBudgetDto> categoryBudgets) {
        this.monthlyBudget = monthlyBudget;
        this.categoryBudgets = categoryBudgets;
    }

    public BigDecimal getMonthlyBudget() { return monthlyBudget; }
    public List<CategoryBudgetDto> getCategoryBudgets() { return categoryBudgets; }
}
