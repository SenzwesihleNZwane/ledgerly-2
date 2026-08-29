package com.ledgerly.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * A group of people who share expenses together, e.g. a trip, a household,
 * or a project. Named "Group" - table name is escaped since GROUP is a
 * reserved SQL keyword.
 */
@Entity
@Table(name = "expense_group")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany
    @JoinTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    public Group() {
    }

    public Group(String name, User owner) {
        this.name = name;
        this.owner = owner;
        this.members.add(owner);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Set<User> getMembers() {
        return members;
    }

    public void setMembers(Set<User> members) {
        this.members = members;
    }

    public boolean hasMember(Long userId) {
        return members.stream().anyMatch(m -> m.getId().equals(userId));
    }
}
