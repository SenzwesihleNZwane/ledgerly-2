package com.ledgerly.controller;

import com.ledgerly.dto.BalanceResponse;
import com.ledgerly.model.User;
import com.ledgerly.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups/{groupId}/balances")
public class BalanceController {

    @Autowired
    private BalanceService balanceService;

    @GetMapping
    public ResponseEntity<BalanceResponse> getBalances(@PathVariable Long groupId,
                                                          @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(balanceService.getBalances(groupId, currentUser));
    }
}
