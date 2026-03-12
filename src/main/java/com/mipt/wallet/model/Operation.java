package com.mipt.wallet.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "operations")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Operation {
  @Id
  @Column(name = "id")
  UUID id;

  @Column(name = "client")
  UUID clientId;

  @Column(name = "performer")
  UUID performerId;

  @Column(name = "amount")
  Long amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  OperationType type;

  @Column(name = "time")
  Instant time;

  @Column(name = "title")
  String title;
}
