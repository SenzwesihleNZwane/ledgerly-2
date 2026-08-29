package com.ledgerly.dto;

import java.math.BigDecimal;

/**
 * One entry in an incoming AddExpenseRequest: which user, and (for EXACT
 * splits) how much they owe. For EQUAL splits only userId is needed.
 */
public class SplitInput {
    private Long userId;
    private BigDecimal amount;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
