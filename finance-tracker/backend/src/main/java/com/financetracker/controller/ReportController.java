package com.financetracker.controller;

import com.financetracker.dto.ApiResponse;
import com.financetracker.dto.FinanceSummary;
import com.financetracker.dto.TransactionResponse;
import com.financetracker.model.Budget;
import com.financetracker.model.Transaction;
import com.financetracker.repository.BudgetRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.security.UserPrincipal;
import com.financetracker.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final ReportService reportService;

    public ReportController(TransactionRepository transactionRepository, BudgetRepository budgetRepository,
                             ReportService reportService) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<?> monthly(@AuthenticationPrincipal UserPrincipal principal,
                                      @RequestParam(required = false) Integer year,
                                      @RequestParam(required = false) Integer month,
                                      @RequestParam(required = false, defaultValue = "json") String format) {

        LocalDate now = LocalDate.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();

        YearMonth ym = YearMonth.of(y, m);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Transaction> monthTransactions = transactionRepository
                .findByUserIdAndTransactionDateBetween(principal.getId(), start, end);
        Budget budget = budgetRepository.findByUserId(principal.getId()).orElse(null);

        FinanceSummary summary = reportService.buildMonthlySummary(monthTransactions, budget);

        if ("pdf".equalsIgnoreCase(format)) {
            byte[] pdf = reportService.buildPdf(y, m, monthTransactions, summary);
            String filename = "finance-report-%d-%02d.pdf".formatted(y, m);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        }

        List<TransactionResponse> transactions = monthTransactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("year", y);
        data.put("month", m);
        data.put("totalIncome", summary.getTotalIncome());
        data.put("totalExpense", summary.getTotalExpense());
        data.put("savings", summary.getSavings());
        data.put("budgetRemaining", summary.getBudgetRemaining());
        data.put("categoryBreakdown", summary.getCategoryBreakdown());
        data.put("transactions", transactions);

        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
