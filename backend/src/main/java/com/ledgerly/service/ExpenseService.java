package com.ledgerly.service;

import com.ledgerly.dto.AddExpenseRequest;
import com.ledgerly.dto.ExpenseResponse;
import com.ledgerly.dto.SplitInput;
import com.ledgerly.dto.SplitResponse;
import com.ledgerly.model.Expense;
import com.ledgerly.model.ExpenseSplit;
import com.ledgerly.model.Group;
import com.ledgerly.model.User;
import com.ledgerly.repository.ExpenseRepository;
import com.ledgerly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupService groupService;

    public ExpenseResponse addExpense(Long groupId, AddExpenseRequest request, User currentUser) {
        Group group = groupService.loadGroupForMember(groupId, currentUser);

        User paidBy = userRepository.findById(request.getPaidByUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "paidByUserId not found"));
        if (!group.hasMember(paidBy.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "paidBy user is not a member of this group");
        }

        Expense.SplitType splitType;
        try {
            splitType = Expense.SplitType.valueOf(request.getSplitType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "splitType must be EQUAL or EXACT");
        }

        Expense expense = new Expense();
        expense.setGroup(group);
        expense.setPaidBy(paidBy);
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setSplitType(splitType);

        List<ExpenseSplit> splits = splitType == Expense.SplitType.EQUAL
                ? buildEqualSplits(request, group)
                : buildExactSplits(request, group);

        for (ExpenseSplit split : splits) {
            expense.addSplit(split);
        }

        expenseRepository.save(expense);
        return toResponse(expense);
    }

    public List<ExpenseResponse> getExpenses(Long groupId, User currentUser) {
        groupService.loadGroupForMember(groupId, currentUser);
        return expenseRepository.findByGroup_IdOrderByCreatedAtDesc(groupId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Divides the total amount evenly across all listed participants.
     * Any leftover cents from rounding (e.g. R100 / 3) are added to the
     * first participant's share so the splits always sum exactly to the
     * total - this avoids the classic "off by one cent" bug.
     */
    private List<ExpenseSplit> buildEqualSplits(AddExpenseRequest request, Group group) {
        List<SplitInput> participants = request.getSplits();
        if (participants.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one participant is required");
        }

        int n = participants.size();
        BigDecimal share = request.getAmount()
                .divide(BigDecimal.valueOf(n), 2, RoundingMode.DOWN);
        BigDecimal distributed = share.multiply(BigDecimal.valueOf(n));
        BigDecimal remainder = request.getAmount().subtract(distributed);

        List<ExpenseSplit> splits = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            User participant = resolveMember(participants.get(i).getUserId(), group);
            BigDecimal amount = (i == 0) ? share.add(remainder) : share;
            splits.add(new ExpenseSplit(participant, amount));
        }
        return splits;
    }

    /** Uses the exact amounts supplied by the client; they must sum to the total. */
    private List<ExpenseSplit> buildExactSplits(AddExpenseRequest request, Group group) {
        List<SplitInput> inputs = request.getSplits();
        BigDecimal sum = BigDecimal.ZERO;
        List<ExpenseSplit> splits = new ArrayList<>();

        for (SplitInput input : inputs) {
            if (input.getAmount() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Each split needs an amount when splitType is EXACT");
            }
            User participant = resolveMember(input.getUserId(), group);
            splits.add(new ExpenseSplit(participant, input.getAmount()));
            sum = sum.add(input.getAmount());
        }

        if (sum.setScale(2, RoundingMode.HALF_UP).compareTo(request.getAmount().setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Exact splits (" + sum + ") must add up to the total amount (" + request.getAmount() + ")");
        }
        return splits;
    }

    private User resolveMember(Long userId, Group group) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each split needs a userId");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + userId + " not found"));
        if (!group.hasMember(user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    user.getName() + " is not a member of this group");
        }
        return user;
    }

    private ExpenseResponse toResponse(Expense expense) {
        List<SplitResponse> splits = expense.getSplits().stream()
                .map(s -> new SplitResponse(s.getUser().getId(), s.getUser().getName(), s.getAmountOwed()))
                .toList();
        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getSplitType().name(),
                expense.getPaidBy().getId(),
                expense.getPaidBy().getName(),
                expense.getCreatedAt(),
                splits
        );
    }
}
