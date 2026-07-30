package com.financetracker.dto;

import java.math.BigDecimal;
import java.util.List;

public class BudgetRequest {
    private BigDecimal monthlyBudget;
    private List<CategoryBudgetDto> categoryBudgets;

    public BigDecimal getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(BigDecimal monthlyBudget) { this.monthlyBudget = monthlyBudget; }
    public List<CategoryBudgetDto> getCategoryBudgets() { return categoryBudgets; }
    public void setCategoryBudgets(List<CategoryBudgetDto> categoryBudgets) { this.categoryBudgets = categoryBudgets; }
}
