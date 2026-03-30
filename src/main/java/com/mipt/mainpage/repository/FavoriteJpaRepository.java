package com.mipt.mainpage.repository;

import com.mipt.mainpage.model.Favorite;
import com.mipt.mainpage.model.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteJpaRepository extends JpaRepository<Favorite, FavoriteId> {

  boolean existsByUserIdAndAdvertisementId(UUID userId, UUID advertisementId);

  Optional<Favorite> findByUserIdAndAdvertisementId(UUID userId, UUID advertisementId);

  List<Favorite> findByUserId(UUID userId);

  long countByUserId(UUID userId);

  long countByAdvertisementId(UUID advertisementId);

  void deleteByUserIdAndAdvertisementId(UUID userId, UUID advertisementId);

  void deleteByUserId(UUID userId);
}
