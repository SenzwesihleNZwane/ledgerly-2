package com.ledgerly.dto;

import java.util.List;

public class GroupResponse {
    private Long id;
    private String name;
    private Long ownerId;
    private List<MemberResponse> members;

    public GroupResponse(Long id, String name, Long ownerId, List<MemberResponse> members) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.members = members;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Long getOwnerId() { return ownerId; }
    public List<MemberResponse> getMembers() { return members; }
}
