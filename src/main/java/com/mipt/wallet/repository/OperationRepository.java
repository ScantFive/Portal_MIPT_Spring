package com.mipt.wallet.repository;

import com.mipt.wallet.model.Operation;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationRepository extends JpaRepository<Operation, UUID> {
  List<Operation> findByClientIdAndPerformerId(UUID clientId, UUID performerId);
}
