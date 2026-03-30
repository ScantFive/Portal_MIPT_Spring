package com.mipt.wallet.repository;

import com.mipt.util.SpringContext;
import com.mipt.wallet.model.Operation;
import java.util.List;
import java.util.UUID;

public class OperationRepository {

  private OperationJpaRepository repository() {
    return SpringContext.getBean(OperationJpaRepository.class);
  }

  public Operation create(Operation operation) {
    return repository().save(operation);
  }

  public boolean existsById(UUID id) {
    return repository().existsById(id);
  }

  public Operation findById(UUID id) {
    return repository().findById(id)
        .orElseThrow(() -> new RuntimeException("No operation with this ID."));
  }

  public List<Operation> findByParticipantsId(UUID clientId, UUID performerId) {
    return repository().findByParticipantsId(clientId, performerId);
  }

  public List<Operation> findAll() {
    return repository().findAll();
  }

  public void update(Operation newOperation) {
    repository().save(newOperation);
  }

  public void deleteById(UUID id) {
    repository().deleteById(id);
  }
}
