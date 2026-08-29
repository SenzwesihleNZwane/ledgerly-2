package com.ledgerly.dto;

import java.math.BigDecimal;

public class SplitResponse {
    private Long userId;
    private String userName;
    private BigDecimal amountOwed;

    public SplitResponse(Long userId, String userName, BigDecimal amountOwed) {
        this.userId = userId;
        this.userName = userName;
        this.amountOwed = amountOwed;
    }

    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public BigDecimal getAmountOwed() { return amountOwed; }
}
