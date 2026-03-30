package com.mipt.search.repository;

import com.mipt.search.model.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SearchHistoryJpaRepository extends JpaRepository<SearchHistory, UUID> {

  List<SearchHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);

  void deleteByUserId(UUID userId);

  void deleteById(UUID id);
}
