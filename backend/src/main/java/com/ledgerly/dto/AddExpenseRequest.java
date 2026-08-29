package com.ledgerly.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class AddExpenseRequest {
    @NotBlank(message = "Description is required")
    private String description;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "paidByUserId is required")
    private Long paidByUserId;

    @NotBlank(message = "splitType must be EQUAL or EXACT")
    private String splitType;

    @NotEmpty(message = "At least one split participant is required")
    private List<SplitInput> splits;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Long getPaidByUserId() { return paidByUserId; }
    public void setPaidByUserId(Long paidByUserId) { this.paidByUserId = paidByUserId; }
    public String getSplitType() { return splitType; }
    public void setSplitType(String splitType) { this.splitType = splitType; }
    public List<SplitInput> getSplits() { return splits; }
    public void setSplits(List<SplitInput> splits) { this.splits = splits; }
}
