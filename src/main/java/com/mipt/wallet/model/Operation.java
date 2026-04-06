package com.mipt.wallet.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "operations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Operation {

  @Id
  @Column(name = "id")
  @Builder.Default
  private UUID id = UUID.randomUUID();

  @Column(name = "client", nullable = false)
  private UUID clientId;

  @Column(name = "performer", nullable = false)
  private UUID performerId;

  @Column(name = "amount", nullable = false)
  private Long amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private OperationType type;

  @Column(name = "time", nullable = false)
  @Builder.Default
  private Instant time = Instant.now();

  @Column(name = "title", length = 500)
  private String title;
}
