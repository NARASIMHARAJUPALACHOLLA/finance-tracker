package com.financetracker.controller;

import com.financetracker.dto.ApiResponse;
import com.financetracker.dto.DashboardSummaryResponse;
import com.financetracker.dto.FinanceSummary;
import com.financetracker.dto.TransactionResponse;
import com.financetracker.model.Budget;
import com.financetracker.model.Transaction;
import com.financetracker.repository.BudgetRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.security.UserPrincipal;
import com.financetracker.service.FinanceAnalyzerService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final FinanceAnalyzerService financeAnalyzerService;

    public DashboardController(TransactionRepository transactionRepository, BudgetRepository budgetRepository,
                                FinanceAnalyzerService financeAnalyzerService) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.financeAnalyzerService = financeAnalyzerService;
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> summary(@AuthenticationPrincipal UserPrincipal principal) {
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(principal.getId());
        Budget budget = budgetRepository.findByUserId(principal.getId()).orElse(null);
        FinanceSummary summary = financeAnalyzerService.analyze(transactions, budget);

        // transactions are already sorted desc by date, so the first 5 are the most recent
        List<TransactionResponse> recent = transactions.stream()
                .limit(5)
                .map(TransactionResponse::new)
                .collect(Collectors.toList());

        return ApiResponse.ok(new DashboardSummaryResponse(summary, recent));
    }
}
