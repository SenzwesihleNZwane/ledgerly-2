package com.ledgerly.repository;

import com.ledgerly.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByMembers_Id(Long userId);
}
