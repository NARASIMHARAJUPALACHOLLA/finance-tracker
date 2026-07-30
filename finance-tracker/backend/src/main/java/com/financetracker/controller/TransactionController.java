package com.financetracker.controller;

import com.financetracker.dto.ApiResponse;
import com.financetracker.dto.TransactionListResponse;
import com.financetracker.dto.TransactionRequest;
import com.financetracker.dto.TransactionResponse;
import com.financetracker.security.UserPrincipal;
import com.financetracker.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ApiResponse<TransactionResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                     @Valid @RequestBody TransactionRequest req) {
        return ApiResponse.ok("Transaction added", transactionService.create(principal.getId(), req));
    }

    @GetMapping
    public ApiResponse<TransactionListResponse> list(@AuthenticationPrincipal UserPrincipal principal,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int limit,
                                                       @RequestParam(required = false) String category,
                                                       @RequestParam(required = false) String type,
                                                       @RequestParam(required = false) String search,
                                                       @RequestParam(required = false) String sort) {
        return ApiResponse.ok(transactionService.list(principal.getId(), page, limit, category, type, search, sort));
    }

    @PutMapping("/{id}")
    public ApiResponse<TransactionResponse> update(@AuthenticationPrincipal UserPrincipal principal,
                                                     @PathVariable Long id,
                                                     @Valid @RequestBody TransactionRequest req) {
        return ApiResponse.ok("Transaction updated", transactionService.update(principal.getId(), id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        transactionService.delete(principal.getId(), id);
        return ApiResponse.ok("Transaction deleted", null);
    }
}
