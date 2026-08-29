package com.ledgerly.controller;

import com.ledgerly.dto.AddMemberRequest;
import com.ledgerly.dto.CreateGroupRequest;
import com.ledgerly.dto.GroupResponse;
import com.ledgerly.model.User;
import com.ledgerly.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request,
                                                       @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(groupService.createGroup(request, currentUser));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getMyGroups(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(groupService.getGroupsForUser(currentUser));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable Long groupId,
                                                    @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(groupService.getGroup(groupId, currentUser));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupResponse> addMember(@PathVariable Long groupId,
                                                     @Valid @RequestBody AddMemberRequest request,
                                                     @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(groupService.addMember(groupId, request.getEmail(), currentUser));
    }
}
