package com.financetracker.service;

import com.financetracker.dto.*;
import com.financetracker.exception.ApiException;
import com.financetracker.model.Budget;
import com.financetracker.model.Transaction;
import com.financetracker.model.User;
import com.financetracker.repository.BudgetRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import com.financetracker.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final FinanceAnalyzerService financeAnalyzerService;

    public AuthService(UserRepository userRepository, TransactionRepository transactionRepository,
                        BudgetRepository budgetRepository, PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil, FinanceAnalyzerService financeAnalyzerService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.financeAnalyzerService = financeAnalyzerService;
    }

    public AuthResponse signup(SignupRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ApiException("An account with this email already exists", HttpStatus.BAD_REQUEST);
        }
        User user = new User(req.getName().trim(), email, passwordEncoder.encode(req.getPassword()));
        user = userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, toResponse(user));
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, toResponse(user));
    }

    public UserResponse getMe(Long userId) {
        User user = findUser(userId);
        return toResponse(user);
    }

    public UserResponse updateProfile(Long userId, UpdateProfileRequest req) {
        User user = findUser(userId);
        String email = req.getEmail().trim().toLowerCase();

        userRepository.findByEmail(email).ifPresent(existing -> {
            if (!existing.getId().equals(userId)) {
                throw new ApiException("This email is already in use", HttpStatus.BAD_REQUEST);
            }
        });

        user.setName(req.getName().trim());
        user.setEmail(email);
        user = userRepository.save(user);
        return toResponse(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest req) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new ApiException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    public AccountStatsResponse getAccountStats(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
        Budget budget = budgetRepository.findByUserId(userId).orElse(null);
        FinanceSummary summary = financeAnalyzerService.analyze(transactions, budget);
        return new AccountStatsResponse(
                transactions.size(),
                summary.getTotalIncome() == null ? BigDecimal.ZERO : summary.getTotalIncome(),
                summary.getTotalExpense() == null ? BigDecimal.ZERO : summary.getTotalExpense(),
                summary.getSavings() == null ? BigDecimal.ZERO : summary.getSavings()
        );
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
