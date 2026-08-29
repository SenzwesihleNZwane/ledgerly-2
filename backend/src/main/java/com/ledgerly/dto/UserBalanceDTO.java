package com.ledgerly.dto;

import java.math.BigDecimal;

/**
 * A user's net position in a group: positive means the group owes them
 * money overall, negative means they owe the group money overall.
 */
public class UserBalanceDTO {
    private Long userId;
    private String name;
    private BigDecimal netAmount;

    public UserBalanceDTO(Long userId, String name, BigDecimal netAmount) {
        this.userId = userId;
        this.name = name;
        this.netAmount = netAmount;
    }

    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public BigDecimal getNetAmount() { return netAmount; }
}
