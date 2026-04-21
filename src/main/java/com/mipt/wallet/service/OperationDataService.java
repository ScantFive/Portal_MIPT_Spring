package com.mipt.wallet.service;

import com.mipt.wallet.model.Operation;
import com.mipt.wallet.repository.OperationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OperationDataService {

  private final OperationJpaRepository repository;

  public Operation create(Operation operation) {
    return repository.save(operation);
  }

  public boolean existsById(UUID id) {
    return repository.existsById(id);
  }

  public Operation findById(UUID id) {
    return repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Operation not found: " + id));
  }

  public List<Operation> findByParticipantsId(UUID clientId, UUID performerId) {
    return repository.findByParticipantsId(clientId, performerId);
  }

  public List<Operation> findByUserId(UUID userId) {
    return repository.findByUserId(userId);
  }

  public List<Operation> findAll() {
    return repository.findAll();
  }

  public void update(Operation newOperation) {
    repository.save(newOperation);
  }

  public void deleteById(UUID id) {
    repository.deleteById(id);
  }
}
