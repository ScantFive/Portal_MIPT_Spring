package com.mipt.mainpage.event;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MainPageEvent {
 private String eventType;
 private UUID userId;
 private UUID advertisementId;
 private String action;
 private String details;
 private Instant timestamp;

 public static MainPageEvent favoriteAdded(UUID userId, UUID advertisementId) {
  return MainPageEvent.builder()
    .eventType("MAINPAGE_FAVORITE_ADDED")
    .userId(userId)
    .advertisementId(advertisementId)
    .action("FAVORITE_ADD")
    .details("Advertisement added to favorites")
    .timestamp(Instant.now())
    .build();
 }

 public static MainPageEvent favoriteRemoved(UUID userId, UUID advertisementId) {
  return MainPageEvent.builder()
    .eventType("MAINPAGE_FAVORITE_REMOVED")
    .userId(userId)
    .advertisementId(advertisementId)
    .action("FAVORITE_REMOVE")
    .details("Advertisement removed from favorites")
    .timestamp(Instant.now())
    .build();
 }
}
