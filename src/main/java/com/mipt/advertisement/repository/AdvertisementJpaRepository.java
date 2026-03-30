package com.mipt.advertisement.repository;

import com.mipt.advertisement.model.Advertisement;
import com.mipt.advertisement.model.AdvertisementStatus;
import com.mipt.advertisement.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdvertisementJpaRepository extends JpaRepository<Advertisement, UUID> {

  List<Advertisement> findByAuthorId(UUID authorId);

  List<Advertisement> findByStatus(AdvertisementStatus status);

  List<Advertisement> findByCategory(Category category);

  List<Advertisement> findByIsFavoriteTrue();

  @Query("SELECT DISTINCT a.category FROM Advertisement a WHERE a.category IS NOT NULL")
  List<Category> findAllCategories();

  long countByStatus(AdvertisementStatus status);
}
