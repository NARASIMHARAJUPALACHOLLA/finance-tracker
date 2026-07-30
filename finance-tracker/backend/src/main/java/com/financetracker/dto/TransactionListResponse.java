package com.financetracker.dto;

import java.util.List;

public class TransactionListResponse {
    private List<TransactionResponse> transactions;
    private PaginationMeta pagination;

    public TransactionListResponse(List<TransactionResponse> transactions, PaginationMeta pagination) {
        this.transactions = transactions;
        this.pagination = pagination;
    }

    public List<TransactionResponse> getTransactions() { return transactions; }
    public PaginationMeta getPagination() { return pagination; }
}
