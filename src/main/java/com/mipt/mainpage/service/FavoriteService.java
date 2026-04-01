package com.mipt.mainpage.service;

import com.mipt.mainpage.event.MainPageEvent;
import com.mipt.mainpage.model.Favorite;
import com.mipt.mainpage.repository.FavoriteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteService {

  private final FavoriteJpaRepository repository;
  private final MainPageKafkaEventPublisher eventPublisher;

  public boolean isFavorite(UUID userId, UUID advertisementId) {
    return repository.existsByUserIdAndAdvertisementId(userId, advertisementId);
  }

  public void addToFavorites(UUID userId, UUID advertisementId) {
    if (!isFavorite(userId, advertisementId)) {
      Favorite favorite = Favorite.builder()
          .userId(userId)
          .advertisementId(advertisementId)
          .build();
      repository.save(favorite);
      eventPublisher.publish(MainPageEvent.favoriteAdded(userId, advertisementId));
    }
  }

  public void removeFromFavorites(UUID userId, UUID advertisementId) {
    repository.deleteByUserIdAndAdvertisementId(userId, advertisementId);
    eventPublisher.publish(MainPageEvent.favoriteRemoved(userId, advertisementId));
  }

  public long getFavoritesCount(UUID userId) {
    return repository.countByUserId(userId);
  }

  public long getFavoriteUsersCount(UUID advertisementId) {
    return repository.countByAdvertisementId(advertisementId);
  }
}
