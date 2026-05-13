package com.nexus.server.repository;

import com.nexus.server.model.ChatRequest;
import com.nexus.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRequestRepository extends JpaRepository<ChatRequest, Long> {
    List<ChatRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<ChatRequest> findByUserOrderByCreatedAtDesc(User user);
}
