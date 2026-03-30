package com.mipt.mainpage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "favorites", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "advertisement_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorite {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id")
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "advertisement_id", nullable = false)
  private UUID advertisementId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }
}
