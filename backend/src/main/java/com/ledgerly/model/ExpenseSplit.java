package com.ledgerly.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * One line of a split: "this user owes this much of this expense".
 * An Expense has one ExpenseSplit per participant.
 */
@Entity
@Table(name = "expense_split")
public class ExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountOwed;

    public ExpenseSplit() {
    }

    public ExpenseSplit(User user, BigDecimal amountOwed) {
        this.user = user;
        this.amountOwed = amountOwed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Expense getExpense() {
        return expense;
    }

    public void setExpense(Expense expense) {
        this.expense = expense;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getAmountOwed() {
        return amountOwed;
    }

    public void setAmountOwed(BigDecimal amountOwed) {
        this.amountOwed = amountOwed;
    }
}
