package com.mipt.advertisement.repository;

import com.mipt.advertisement.model.Advertisement;
import com.mipt.advertisement.model.AdvertisementStatus;
import com.mipt.advertisement.model.Category;
import com.mipt.advertisement.model.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, UUID> {

  // ===== Базовые методы поиска (Spring Data JPA реализует автоматически) =====

  List<Advertisement> findByAuthorId(UUID authorId);

  List<Advertisement> findByStatus(AdvertisementStatus status);

  List<Advertisement> findByCategory(Category category);

  List<Advertisement> findByType(Type type);

  List<Advertisement> findByIsFavoriteTrue();

  Optional<Advertisement> findById(UUID id);

  boolean existsById(UUID id);

  // ===== Методы с пагинацией и сортировкой =====

  List<Advertisement> findAllByOrderByCreatedAtDesc();

  List<Advertisement> findByStatusOrderByCreatedAtDesc(AdvertisementStatus status);

  // ===== Кастомные запросы через @Query =====

  @Query("SELECT a FROM Advertisement a WHERE a.status = 'ACTIVE' AND a.category = :category")
  List<Advertisement> findActiveByCategory(@Param("category") Category category);

  @Query("SELECT a FROM Advertisement a WHERE a.status = 'ACTIVE' AND a.price BETWEEN :minPrice AND :maxPrice")
  List<Advertisement> findInPriceRange(@Param("minPrice") Long minPrice, @Param("maxPrice") Long maxPrice);

  @Query("SELECT a FROM Advertisement a WHERE a.status = 'ACTIVE' AND LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  List<Advertisement> searchByName(@Param("keyword") String keyword);

  // Полнотекстовый поиск (native query, использует вашу search_vector колонку)
  @Query(value = """
        SELECT * FROM advertisements a 
        WHERE a.status = 'ACTIVE' 
        AND a.search_vector @@ plainto_tsquery('russian', :query)
        ORDER BY ts_rank(a.search_vector, plainto_tsquery('russian', :query)) DESC
        """, nativeQuery = true)
  List<Advertisement> fullTextSearch(@Param("query") String query);

  // ===== Методы для обновления (требуют @Modifying и @Transactional) =====

  @Modifying
  @Transactional
  @Query("UPDATE Advertisement a SET a.price = :price WHERE a.id = :id")
  int updatePrice(@Param("id") UUID id, @Param("price") Long price);

  @Modifying
  @Transactional
  @Query(value = "UPDATE advertisements SET is_favorite = NOT is_favorite WHERE id = :id", nativeQuery = true)
  int toggleFavorite(@Param("id") UUID id);

  @Modifying
  @Transactional
  @Query("UPDATE Advertisement a SET a.status = :status WHERE a.id = :id")
  int updateStatus(@Param("id") UUID id, @Param("status") AdvertisementStatus status);

  // ===== Агрегационные методы =====

  long countByStatus(AdvertisementStatus status);

  long countByAuthorId(UUID authorId);

  @Query("SELECT AVG(a.price) FROM Advertisement a WHERE a.status = 'ACTIVE' AND a.category = :category")
  Double getAveragePriceByCategory(@Param("category") Category category);

  // ===== Проверочные методы =====
}