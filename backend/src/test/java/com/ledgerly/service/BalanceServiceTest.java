package com.ledgerly.service;

import com.ledgerly.dto.BalanceResponse;
import com.ledgerly.dto.SettlementDTO;
import com.ledgerly.model.Expense;
import com.ledgerly.model.ExpenseSplit;
import com.ledgerly.model.Group;
import com.ledgerly.model.User;
import com.ledgerly.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Tests the core debt-simplification algorithm in isolation, since that's
 * the piece of logic worth proving works correctly.
 */
@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private GroupService groupService;

    @InjectMocks
    private BalanceService balanceService;

    private User alice, bob, carol;
    private Group group;

    @BeforeEach
    void setUp() {
        alice = new User("Alice", "alice@test.com", "x");
        alice.setId(1L);
        bob = new User("Bob", "bob@test.com", "x");
        bob.setId(2L);
        carol = new User("Carol", "carol@test.com", "x");
        carol.setId(3L);

        group = new Group("Trip", alice);
        group.setId(10L);
        group.getMembers().add(bob);
        group.getMembers().add(carol);

        when(groupService.loadGroupForMember(10L, alice)).thenReturn(group);
    }

    @Test
    void oneExpensePaidByOneUser_producesTwoSettlementsBackToThatUser() {
        // Alice pays 90, split equally three ways (30 each)
        Expense dinner = new Expense();
        dinner.setId(100L);
        dinner.setGroup(group);
        dinner.setPaidBy(alice);
        dinner.setDescription("Dinner");
        dinner.setAmount(new BigDecimal("90.00"));
        dinner.setSplitType(Expense.SplitType.EQUAL);
        dinner.addSplit(new ExpenseSplit(alice, new BigDecimal("30.00")));
        dinner.addSplit(new ExpenseSplit(bob, new BigDecimal("30.00")));
        dinner.addSplit(new ExpenseSplit(carol, new BigDecimal("30.00")));

        when(expenseRepository.findByGroup_IdOrderByCreatedAtDesc(10L)).thenReturn(List.of(dinner));

        BalanceResponse response = balanceService.getBalances(10L, alice);

        // Alice is owed 60 total, Bob and Carol each owe 30
        assertEquals(2, response.getSettlements().size());
        BigDecimal totalSettled = response.getSettlements().stream()
                .map(SettlementDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("60.00"), totalSettled);

        response.getSettlements().forEach(s -> assertEquals("Alice", s.getToName()));
    }

    @Test
    void everyoneEven_producesNoSettlements() {
        // Alice pays 30, split equally between Alice and Bob only -> exactly even
        Expense coffee = new Expense();
        coffee.setId(101L);
        coffee.setGroup(group);
        coffee.setPaidBy(alice);
        coffee.setDescription("Coffee");
        coffee.setAmount(new BigDecimal("30.00"));
        coffee.setSplitType(Expense.SplitType.EQUAL);
        coffee.addSplit(new ExpenseSplit(alice, new BigDecimal("15.00")));
        coffee.addSplit(new ExpenseSplit(bob, new BigDecimal("15.00")));

        Expense repay = new Expense();
        repay.setId(102L);
        repay.setGroup(group);
        repay.setPaidBy(bob);
        repay.setDescription("Bob pays Alice back");
        repay.setAmount(new BigDecimal("15.00"));
        repay.setSplitType(Expense.SplitType.EQUAL);
        repay.addSplit(new ExpenseSplit(alice, new BigDecimal("15.00")));

        when(expenseRepository.findByGroup_IdOrderByCreatedAtDesc(10L)).thenReturn(List.of(coffee, repay));

        BalanceResponse response = balanceService.getBalances(10L, alice);

        assertEquals(0, response.getSettlements().size());
    }
}
