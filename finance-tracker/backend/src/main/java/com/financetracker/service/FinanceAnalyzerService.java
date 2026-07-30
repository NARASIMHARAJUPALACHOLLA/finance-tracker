package com.financetracker.service;

import com.financetracker.dto.*;
import com.financetracker.model.Budget;
import com.financetracker.model.CategoryBudget;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pure, side-effect-free analytics engine. Given a user's transactions and
 * (optional) budget it always produces the same deterministic summary --
 * totals, savings, category breakdown, monthly trend, a predicted expense,
 * a confidence score, and rule-based insights. This is the safety net the
 * AI layer falls back to, and the single source of truth reused by the
 * dashboard, reports, and account-stats endpoints.
 */
@Service
public class FinanceAnalyzerService {

    private static final DateTimeFormatter MONTH_KEY = DateTimeFormatter.ofPattern("yyyy-MM");

    public FinanceSummary analyze(List<Transaction> transactions, Budget budget) {
        FinanceSummary summary = new FinanceSummary();
        summary.setTransactionCount(transactions.size());

        BigDecimal totalIncome = sum(transactions, TransactionType.INCOME);
        BigDecimal totalExpense = sum(transactions, TransactionType.EXPENSE);
        BigDecimal savings = totalIncome.subtract(totalExpense);
        BigDecimal monthlyBudget = budget != null && budget.getMonthlyBudget() != null
                ? budget.getMonthlyBudget() : BigDecimal.ZERO;
        BigDecimal budgetRemaining = monthlyBudget.subtract(totalExpense);

        summary.setTotalIncome(totalIncome);
        summary.setTotalExpense(totalExpense);
        summary.setSavings(savings);
        summary.setBudgetRemaining(budgetRemaining);

        // Category breakdown, highest spend first
        Map<String, BigDecimal> byCategory = new LinkedHashMap<>();
        transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .forEach(t -> byCategory.merge(t.getCategory(), t.getAmount(), BigDecimal::add));

        List<CategoryAmount> categoryBreakdown = byCategory.entrySet().stream()
                .map(e -> new CategoryAmount(e.getKey(), e.getValue()))
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .collect(Collectors.toList());
        summary.setCategoryBreakdown(categoryBreakdown);

        // Monthly trend -- keys sort lexically the same as chronologically (YYYY-MM)
        Map<String, BigDecimal[]> buckets = new TreeMap<>(); // [income, expense]
        for (Transaction t : transactions) {
            String key = t.getTransactionDate().format(MONTH_KEY);
            BigDecimal[] bucket = buckets.computeIfAbsent(key, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (t.getType() == TransactionType.INCOME) {
                bucket[0] = bucket[0].add(t.getAmount());
            } else {
                bucket[1] = bucket[1].add(t.getAmount());
            }
        }
        List<MonthlyTrendPoint> monthlyTrend = buckets.entrySet().stream()
                .map(e -> new MonthlyTrendPoint(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .collect(Collectors.toList());
        summary.setMonthlyTrend(monthlyTrend);

        // Predicted expense = average of monthly expense buckets (smooths volatility)
        BigDecimal predictedExpense = monthlyTrend.isEmpty()
                ? BigDecimal.ZERO
                : monthlyTrend.stream().map(MonthlyTrendPoint::getExpense).reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(monthlyTrend.size()), 2, RoundingMode.HALF_UP);
        summary.setPredictedExpense(predictedExpense);

        // Confidence climbs 3 points per recorded expense, clamped to [55, 95]
        long expenseCount = transactions.stream().filter(t -> t.getType() == TransactionType.EXPENSE).count();
        int confidence = (int) Math.min(95, Math.max(55, 60 + expenseCount * 3));
        summary.setConfidence(confidence);

        boolean budgetRisk = monthlyBudget.compareTo(BigDecimal.ZERO) > 0
                && predictedExpense.compareTo(monthlyBudget) > 0;
        summary.setBudgetRisk(budgetRisk);

        List<String> insights = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        if (transactions.isEmpty()) {
            insights.add("You haven't logged any transactions yet -- add your first income or expense to unlock insights.");
            recommendations.add("Start by logging today's transactions so trends can build up over the coming weeks.");
        } else {
            if (!categoryBreakdown.isEmpty()) {
                CategoryAmount top = categoryBreakdown.get(0);
                BigDecimal pct = totalExpense.compareTo(BigDecimal.ZERO) > 0
                        ? top.getAmount().multiply(BigDecimal.valueOf(100)).divide(totalExpense, 0, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                insights.add("\"" + top.getCategory() + "\" is your largest expense category at " + pct + "% of total spending.");
            }
            if (savings.compareTo(BigDecimal.ZERO) < 0) {
                insights.add("You're spending more than you're earning this period -- expenses exceed income.");
                recommendations.add("Review discretionary categories first; small cuts there add up fastest.");
            } else {
                BigDecimal savingsRate = totalIncome.compareTo(BigDecimal.ZERO) > 0
                        ? savings.multiply(BigDecimal.valueOf(100)).divide(totalIncome, 0, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                insights.add("You're saving roughly " + savingsRate + "% of your income so far.");
            }
            if (budgetRisk) {
                insights.add("Based on your recent spending pace, you're on track to exceed your monthly budget.");
                recommendations.add("Consider setting a tighter category limit on \"" +
                        (categoryBreakdown.isEmpty() ? "your top category" : categoryBreakdown.get(0).getCategory()) +
                        "\" for the rest of the month.");
            } else if (monthlyBudget.compareTo(BigDecimal.ZERO) > 0) {
                insights.add("You're currently within your monthly budget.");
            }
            if (budget != null) {
                for (CategoryBudget cb : budget.getCategoryBudgets()) {
                    BigDecimal spent = byCategory.getOrDefault(cb.getCategory(), BigDecimal.ZERO);
                    if (cb.getLimit() != null && cb.getLimit().compareTo(BigDecimal.ZERO) > 0
                            && spent.compareTo(cb.getLimit()) > 0) {
                        recommendations.add("You've gone over your \"" + cb.getCategory() + "\" budget -- consider adjusting the limit or cutting back.");
                    }
                }
            }
            if (recommendations.isEmpty()) {
                recommendations.add("Keep logging transactions consistently to sharpen future predictions.");
            }
        }

        summary.setInsights(insights);
        summary.setRecommendations(recommendations);

        return summary;
    }

    private BigDecimal sum(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
