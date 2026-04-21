package com.mipt.wallet.repository;

import com.mipt.wallet.model.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OperationJpaRepository extends JpaRepository<Operation, UUID> {

    @Query("SELECT o FROM Operation o WHERE (o.clientId = :clientId AND o.performerId = :performerId) OR (o.clientId = :performerId AND o.performerId = :clientId)")
    List<Operation> findByParticipantsId(@Param("clientId") UUID clientId,
            @Param("performerId") UUID performerId);

    @Query("SELECT o FROM Operation o WHERE o.clientId = :userId OR o.performerId = :userId ORDER BY o.time DESC")
    List<Operation> findByUserId(@Param("userId") UUID userId);
}
