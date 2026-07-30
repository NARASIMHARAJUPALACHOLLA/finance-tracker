package com.financetracker.dto;

import java.math.BigDecimal;

public class CategoryAmount {
    private String category;
    private BigDecimal amount;

    public CategoryAmount(String category, BigDecimal amount) {
        this.category = category;
        this.amount = amount;
    }

    public String getCategory() { return category; }
    public BigDecimal getAmount() { return amount; }
}
