package com.financetracker.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private BigDecimal monthlyBudget = BigDecimal.ZERO;

    @ElementCollection
    @CollectionTable(name = "category_budgets", joinColumns = @JoinColumn(name = "budget_id"))
    private List<CategoryBudget> categoryBudgets = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public BigDecimal getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(BigDecimal monthlyBudget) { this.monthlyBudget = monthlyBudget; }
    public List<CategoryBudget> getCategoryBudgets() { return categoryBudgets; }
    public void setCategoryBudgets(List<CategoryBudget> categoryBudgets) { this.categoryBudgets = categoryBudgets; }
}
