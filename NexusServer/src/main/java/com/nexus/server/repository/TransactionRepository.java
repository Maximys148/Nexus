package com.nexus.server.repository;

import com.nexus.server.model.Transaction;
import com.nexus.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Transaction> findByUserOrderByCreatedAtDesc(User user);
}
