package com.ledgerly.controller;

import com.ledgerly.dto.AddExpenseRequest;
import com.ledgerly.dto.ExpenseResponse;
import com.ledgerly.model.User;
import com.ledgerly.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> addExpense(@PathVariable Long groupId,
                                                        @Valid @RequestBody AddExpenseRequest request,
                                                        @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(expenseService.addExpense(groupId, request, currentUser));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getExpenses(@PathVariable Long groupId,
                                                                @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(expenseService.getExpenses(groupId, currentUser));
    }
}
