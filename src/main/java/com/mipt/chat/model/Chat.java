package com.mipt.chat.model;

import java.time.Instant;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "chats")
@Builder
@Data
public class Chat {
  @Id
  @Builder.Default
  final UUID id = UUID.randomUUID();

  @Column(name = "owner")
  UUID ownerId;

  @Column(name = "member")
  UUID memberId;

  @Column(name = "last_update")
  @Builder.Default
  Instant lastUpdate = Instant.now();
}
