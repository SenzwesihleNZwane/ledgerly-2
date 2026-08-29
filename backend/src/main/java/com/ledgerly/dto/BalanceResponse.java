package com.ledgerly.dto;

import java.util.List;

public class BalanceResponse {
    private List<UserBalanceDTO> balances;
    private List<SettlementDTO> settlements;

    public BalanceResponse(List<UserBalanceDTO> balances, List<SettlementDTO> settlements) {
        this.balances = balances;
        this.settlements = settlements;
    }

    public List<UserBalanceDTO> getBalances() { return balances; }
    public List<SettlementDTO> getSettlements() { return settlements; }
}
