package com.ledgerly.service;

import com.ledgerly.dto.BalanceResponse;
import com.ledgerly.dto.SettlementDTO;
import com.ledgerly.dto.UserBalanceDTO;
import com.ledgerly.model.Expense;
import com.ledgerly.model.ExpenseSplit;
import com.ledgerly.model.Group;
import com.ledgerly.model.User;
import com.ledgerly.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Computes each member's net balance in a group, then works out the
 * smallest set of payments needed to settle everyone up.
 *
 * The core idea (this is the "why would a recruiter care" part of the
 * project): rather than everyone paying everyone back individually,
 * we greedily match the person owed the most money against the person
 * who owes the most money, settle between them, and repeat. This keeps
 * the number of transactions close to the theoretical minimum, which is
 * exactly the technique real expense-splitting apps use.
 */
@Service
public class BalanceService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private GroupService groupService;

    private static final BigDecimal EPSILON = new BigDecimal("0.01");

    public BalanceResponse getBalances(Long groupId, User currentUser) {
        Group group = groupService.loadGroupForMember(groupId, currentUser);
        List<Expense> expenses = expenseRepository.findByGroup_IdOrderByCreatedAtDesc(groupId);

        Map<Long, BigDecimal> netByUserId = new HashMap<>();
        Map<Long, User> userById = new HashMap<>();

        for (User member : group.getMembers()) {
            netByUserId.put(member.getId(), BigDecimal.ZERO);
            userById.put(member.getId(), member);
        }

        for (Expense expense : expenses) {
            // whoever paid is owed the full amount back...
            Long payerId = expense.getPaidBy().getId();
            netByUserId.merge(payerId, expense.getAmount(), BigDecimal::add);

            // ...minus what each participant (including the payer, if they
            // were one of the participants) owes for their own share
            for (ExpenseSplit split : expense.getSplits()) {
                Long owerId = split.getUser().getId();
                netByUserId.merge(owerId, split.getAmountOwed().negate(), BigDecimal::add);
            }
        }

        List<UserBalanceDTO> balances = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : netByUserId.entrySet()) {
            User u = userById.get(entry.getKey());
            balances.add(new UserBalanceDTO(u.getId(), u.getName(), entry.getValue().setScale(2, java.math.RoundingMode.HALF_UP)));
        }
        balances.sort((a, b) -> b.getNetAmount().compareTo(a.getNetAmount()));

        List<SettlementDTO> settlements = simplifyDebts(netByUserId, userById);

        return new BalanceResponse(balances, settlements);
    }

    private List<SettlementDTO> simplifyDebts(Map<Long, BigDecimal> netByUserId, Map<Long, User> userById) {
        // Mutable working copy: positive = owed money (creditor), negative = owes money (debtor)
        List<Long> ids = new ArrayList<>(netByUserId.keySet());
        Map<Long, BigDecimal> working = new HashMap<>(netByUserId);

        List<SettlementDTO> settlements = new ArrayList<>();

        while (true) {
            // find the biggest creditor and biggest debtor remaining
            Long maxCreditorId = null;
            Long maxDebtorId = null;
            BigDecimal maxCredit = EPSILON;
            BigDecimal maxDebt = EPSILON;

            for (Long id : ids) {
                BigDecimal amount = working.get(id);
                if (amount.compareTo(maxCredit) > 0) {
                    maxCredit = amount;
                    maxCreditorId = id;
                }
                if (amount.negate().compareTo(maxDebt) > 0) {
                    maxDebt = amount.negate();
                    maxDebtorId = id;
                }
            }

            if (maxCreditorId == null || maxDebtorId == null) {
                break; // everyone is settled (within a cent)
            }

            BigDecimal settleAmount = maxCredit.min(maxDebt).setScale(2, java.math.RoundingMode.HALF_UP);

            settlements.add(new SettlementDTO(
                    maxDebtorId, userById.get(maxDebtorId).getName(),
                    maxCreditorId, userById.get(maxCreditorId).getName(),
                    settleAmount
            ));

            working.put(maxCreditorId, working.get(maxCreditorId).subtract(settleAmount));
            working.put(maxDebtorId, working.get(maxDebtorId).add(settleAmount));
        }

        return settlements;
    }
}
