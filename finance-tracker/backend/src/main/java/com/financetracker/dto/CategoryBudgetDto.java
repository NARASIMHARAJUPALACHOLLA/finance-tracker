package com.financetracker.dto;

import java.math.BigDecimal;

public class CategoryBudgetDto {
    private String category;
    private BigDecimal limit;

    public CategoryBudgetDto() {}

    public CategoryBudgetDto(String category, BigDecimal limit) {
        this.category = category;
        this.limit = limit;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getLimit() { return limit; }
    public void setLimit(BigDecimal limit) { this.limit = limit; }
}
