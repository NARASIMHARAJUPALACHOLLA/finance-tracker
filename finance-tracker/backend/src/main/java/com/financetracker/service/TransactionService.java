package com.financetracker.service;

import com.financetracker.dto.PaginationMeta;
import com.financetracker.dto.TransactionListResponse;
import com.financetracker.dto.TransactionRequest;
import com.financetracker.dto.TransactionResponse;
import com.financetracker.exception.ApiException;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import com.financetracker.model.User;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest req) {
        User user = userRepository.getReferenceById(userId);
        Transaction t = new Transaction();
        t.setUser(user);
        applyRequest(t, req);
        return new TransactionResponse(transactionRepository.save(t));
    }

    public TransactionListResponse list(Long userId, int page, int limit, String category,
                                         String type, String search, String sort) {
        Sort sortOrder = switch (sort == null ? "" : sort) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "transactionDate");
            case "amount_desc" -> Sort.by(Sort.Direction.DESC, "amount");
            case "amount_asc" -> Sort.by(Sort.Direction.ASC, "amount");
            default -> Sort.by(Sort.Direction.DESC, "transactionDate"); // "latest" and unknown values
        };

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, limit), sortOrder);
        TransactionType typeEnum = null;
        if (type != null && !type.isBlank()) {
            try {
                typeEnum = TransactionType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // unknown type filter is ignored, same as an absent one
            }
        }

        String searchTerm = (search == null || search.isBlank()) ? null : search.toLowerCase();
        String categoryFilter = (category == null || category.isBlank()) ? null : category;

        var pageResult = transactionRepository.search(userId, categoryFilter, typeEnum, searchTerm, pageable);

        List<TransactionResponse> transactions = pageResult.getContent().stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());

        PaginationMeta meta = new PaginationMeta(page, limit, pageResult.getTotalElements(), pageResult.getTotalPages());
        return new TransactionListResponse(transactions, meta);
    }

    @Transactional
    public TransactionResponse update(Long userId, Long id, TransactionRequest req) {
        Transaction t = transactionRepository.findById(id)
                .filter(tx -> tx.getUser().getId().equals(userId))
                .orElseThrow(() -> new ApiException("Transaction not found", HttpStatus.NOT_FOUND));
        applyRequest(t, req);
        return new TransactionResponse(transactionRepository.save(t));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Transaction t = transactionRepository.findById(id)
                .filter(tx -> tx.getUser().getId().equals(userId))
                .orElseThrow(() -> new ApiException("Transaction not found", HttpStatus.NOT_FOUND));
        transactionRepository.delete(t);
    }

    private void applyRequest(Transaction t, TransactionRequest req) {
        if (req.getAmount() == null || req.getAmount().signum() <= 0) {
            throw new ApiException("Please provide type, title, amount greater than zero and category", HttpStatus.BAD_REQUEST);
        }
        t.setType(req.getType());
        t.setTitle(req.getTitle().trim());
        t.setAmount(req.getAmount());
        t.setCategory(req.getCategory().trim());
        t.setPaymentMethod(req.getPaymentMethod());
        t.setDescription(req.getDescription());
        t.setTransactionDate(req.getTransactionDate());
    }
}
