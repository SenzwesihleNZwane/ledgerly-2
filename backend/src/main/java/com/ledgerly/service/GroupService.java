package com.ledgerly.service;

import com.ledgerly.dto.CreateGroupRequest;
import com.ledgerly.dto.GroupResponse;
import com.ledgerly.dto.MemberResponse;
import com.ledgerly.model.Group;
import com.ledgerly.model.User;
import com.ledgerly.repository.GroupRepository;
import com.ledgerly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    public GroupResponse createGroup(CreateGroupRequest request, User currentUser) {
        Group group = new Group(request.getName(), currentUser);
        groupRepository.save(group);
        return toResponse(group);
    }

    public List<GroupResponse> getGroupsForUser(User currentUser) {
        return groupRepository.findByMembers_Id(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public GroupResponse getGroup(Long groupId, User currentUser) {
        Group group = loadGroupForMember(groupId, currentUser);
        return toResponse(group);
    }

    public GroupResponse addMember(Long groupId, String memberEmail, User currentUser) {
        Group group = loadGroupForMember(groupId, currentUser);

        User newMember = userRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No user is registered with email " + memberEmail));

        group.getMembers().add(newMember);
        groupRepository.save(group);
        return toResponse(group);
    }

    /**
     * Loads a group and verifies the current user is a member of it.
     * Used internally by this service and by ExpenseService / BalanceService.
     */
    public Group loadGroupForMember(Long groupId, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        if (!group.hasMember(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this group");
        }
        return group;
    }

    private GroupResponse toResponse(Group group) {
        List<MemberResponse> members = group.getMembers().stream()
                .map(m -> new MemberResponse(m.getId(), m.getName(), m.getEmail()))
                .toList();
        return new GroupResponse(group.getId(), group.getName(), group.getOwner().getId(), members);
    }
}
