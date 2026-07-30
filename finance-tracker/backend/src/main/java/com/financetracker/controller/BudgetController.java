package com.financetracker.controller;

import com.financetracker.dto.ApiResponse;
import com.financetracker.dto.BudgetRequest;
import com.financetracker.dto.BudgetResponse;
import com.financetracker.security.UserPrincipal;
import com.financetracker.service.BudgetService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ApiResponse<BudgetResponse> setBudget(@AuthenticationPrincipal UserPrincipal principal,
                                                  @RequestBody BudgetRequest req) {
        return ApiResponse.ok("Budget saved", budgetService.setBudget(principal.getId(), req));
    }

    @GetMapping
    public ApiResponse<BudgetResponse> getBudget(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(budgetService.getBudget(principal.getId()));
    }

    @PutMapping
    public ApiResponse<BudgetResponse> updateBudget(@AuthenticationPrincipal UserPrincipal principal,
                                                      @RequestBody BudgetRequest req) {
        return ApiResponse.ok("Budget updated", budgetService.updateBudget(principal.getId(), req));
    }
}
