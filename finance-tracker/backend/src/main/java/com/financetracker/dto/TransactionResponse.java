package com.financetracker.dto;

import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionResponse {
    private Long id;
    private TransactionType type;
    private String title;
    private BigDecimal amount;
    private String category;
    private String paymentMethod;
    private String description;
    private LocalDate transactionDate;

    public TransactionResponse(Transaction t) {
        this.id = t.getId();
        this.type = t.getType();
        this.title = t.getTitle();
        this.amount = t.getAmount();
        this.category = t.getCategory();
        this.paymentMethod = t.getPaymentMethod();
        this.description = t.getDescription();
        this.transactionDate = t.getTransactionDate();
    }

    public Long getId() { return id; }
    public TransactionType getType() { return type; }
    public String getTitle() { return title; }
    public BigDecimal getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getDescription() { return description; }
    public LocalDate getTransactionDate() { return transactionDate; }
}
