package com.financetracker.controller;

import com.financetracker.dto.*;
import com.financetracker.model.Budget;
import com.financetracker.model.Transaction;
import com.financetracker.repository.BudgetRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.security.UserPrincipal;
import com.financetracker.service.AIService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final AIService aiService;

    public AIController(TransactionRepository transactionRepository, BudgetRepository budgetRepository,
                         AIService aiService) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.aiService = aiService;
    }

    @PostMapping("/insights")
    public ApiResponse<InsightsResponse> insights(@AuthenticationPrincipal UserPrincipal principal) {
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(principal.getId());
        Budget budget = budgetRepository.findByUserId(principal.getId()).orElse(null);
        return ApiResponse.ok(aiService.generateInsights(transactions, budget));
    }

    @PostMapping("/predict")
    public ApiResponse<PredictionResponse> predict(@AuthenticationPrincipal UserPrincipal principal) {
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(principal.getId());
        Budget budget = budgetRepository.findByUserId(principal.getId()).orElse(null);
        FinanceSummary summary = aiService.predictExpense(transactions, budget);
        return ApiResponse.ok(new PredictionResponse(summary.getPredictedExpense(), summary.getConfidence(), summary.isBudgetRisk()));
    }
}
