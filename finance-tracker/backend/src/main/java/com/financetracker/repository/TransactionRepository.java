package com.financetracker.repository;

import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);

    List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDate start, LocalDate end);

    @Query("""
        SELECT t FROM Transaction t WHERE t.user.id = :userId
        AND (:category IS NULL OR t.category = :category)
        AND (:type IS NULL OR t.type = :type)
        AND (:search IS NULL OR LOWER(t.title) LIKE %:search%
             OR LOWER(t.category) LIKE %:search%
             OR LOWER(t.description) LIKE %:search%)
        """)
    Page<Transaction> search(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("type") TransactionType type,
        @Param("search") String search,
        Pageable pageable
    );
}
