package com.financetracker.model;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class CategoryBudget {

    private String category;
    private BigDecimal limit;

    public CategoryBudget() {}

    public CategoryBudget(String category, BigDecimal limit) {
        this.category = category;
        this.limit = limit;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getLimit() { return limit; }
    public void setLimit(BigDecimal limit) { this.limit = limit; }
}
