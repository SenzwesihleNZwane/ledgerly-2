package com.ledgerly.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ExpenseResponse {
    private Long id;
    private String description;
    private BigDecimal amount;
    private String splitType;
    private Long paidByUserId;
    private String paidByName;
    private LocalDateTime createdAt;
    private List<SplitResponse> splits;

    public ExpenseResponse(Long id, String description, BigDecimal amount, String splitType,
                            Long paidByUserId, String paidByName, LocalDateTime createdAt,
                            List<SplitResponse> splits) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.splitType = splitType;
        this.paidByUserId = paidByUserId;
        this.paidByName = paidByName;
        this.createdAt = createdAt;
        this.splits = splits;
    }

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public String getSplitType() { return splitType; }
    public Long getPaidByUserId() { return paidByUserId; }
    public String getPaidByName() { return paidByName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<SplitResponse> getSplits() { return splits; }
}
