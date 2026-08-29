package com.ledgerly.dto;

import java.math.BigDecimal;

/**
 * One suggested payment: fromUser should pay toUser this amount to help
 * settle the group. The full list of these is the minimized settlement plan.
 */
public class SettlementDTO {
    private Long fromUserId;
    private String fromName;
    private Long toUserId;
    private String toName;
    private BigDecimal amount;

    public SettlementDTO(Long fromUserId, String fromName, Long toUserId, String toName, BigDecimal amount) {
        this.fromUserId = fromUserId;
        this.fromName = fromName;
        this.toUserId = toUserId;
        this.toName = toName;
        this.amount = amount;
    }

    public Long getFromUserId() { return fromUserId; }
    public String getFromName() { return fromName; }
    public Long getToUserId() { return toUserId; }
    public String getToName() { return toName; }
    public BigDecimal getAmount() { return amount; }
}
