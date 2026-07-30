package com.financetracker.service;

import com.financetracker.dto.BudgetRequest;
import com.financetracker.dto.BudgetResponse;
import com.financetracker.dto.CategoryBudgetDto;
import com.financetracker.exception.ApiException;
import com.financetracker.model.Budget;
import com.financetracker.model.CategoryBudget;
import com.financetracker.model.User;
import com.financetracker.repository.BudgetRepository;
import com.financetracker.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    public BudgetService(BudgetRepository budgetRepository, UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BudgetResponse setBudget(Long userId, BudgetRequest req) {
        validate(req);
        Budget budget = budgetRepository.findByUserId(userId).orElseGet(() -> {
            Budget b = new Budget();
            b.setUser(userRepository.getReferenceById(userId));
            return b;
        });
        applyRequest(budget, req);
        return toResponse(budgetRepository.save(budget));
    }

    public BudgetResponse getBudget(Long userId) {
        return budgetRepository.findByUserId(userId)
                .map(this::toResponse)
                // No document yet -- return a sane default so the frontend can
                // render the budget form immediately without a "not found" state.
                .orElse(new BudgetResponse(BigDecimal.ZERO, Collections.emptyList()));
    }

    @Transactional
    public BudgetResponse updateBudget(Long userId, BudgetRequest req) {
        validate(req);
        Budget budget = budgetRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("No budget found for this user yet", HttpStatus.NOT_FOUND));
        applyRequest(budget, req);
        return toResponse(budgetRepository.save(budget));
    }

    private void validate(BudgetRequest req) {
        if (req.getMonthlyBudget() == null || req.getMonthlyBudget().signum() < 0) {
            throw new ApiException("Please provide a valid monthly budget", HttpStatus.BAD_REQUEST);
        }
    }

    private void applyRequest(Budget budget, BudgetRequest req) {
        budget.setMonthlyBudget(req.getMonthlyBudget());
        List<CategoryBudget> categoryBudgets = new ArrayList<>();
        if (req.getCategoryBudgets() != null) {
            for (CategoryBudgetDto dto : req.getCategoryBudgets()) {
                categoryBudgets.add(new CategoryBudget(dto.getCategory(), dto.getLimit()));
            }
        }
        budget.setCategoryBudgets(categoryBudgets);
    }

    private BudgetResponse toResponse(Budget budget) {
        List<CategoryBudgetDto> categoryBudgets = budget.getCategoryBudgets().stream()
                .map(cb -> new CategoryBudgetDto(cb.getCategory(), cb.getLimit()))
                .collect(Collectors.toList());
        return new BudgetResponse(budget.getMonthlyBudget(), categoryBudgets);
    }
}
