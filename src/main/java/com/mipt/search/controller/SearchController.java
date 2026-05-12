package com.mipt.search.controller;

import com.mipt.advertisement.controller.dto.AdvertisementResponse;
import com.mipt.search.model.SearchHistory;
import com.mipt.search.model.SearchQuery;
import com.mipt.search.model.SearchSuggestion;
import com.mipt.search.model.SearchType;
import com.mipt.search.service.SearchService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

 private final SearchService searchService;

 @PostMapping
 public List<AdvertisementResponse> search(
         @RequestBody(required = false) SearchQuery query,
         @RequestParam(defaultValue = "50") long limit,
         @RequestParam(defaultValue = "0") long offset,
         @RequestHeader(value = "X-User-Id", required = false) UUID userId) { // Извлечение из заголовка
  return searchService.search(limit, offset, query, userId);
 }

 @GetMapping("/text")
 public List<AdvertisementResponse> searchByText(
         @RequestParam String value,
         @RequestParam(defaultValue = "50") long limit,
         @RequestParam(defaultValue = "0") long offset,
         @RequestHeader(value = "X-User-Id", required = false) UUID userId) { // Добавлен userId
  return searchService.searchByText(value, limit, offset, userId);
 }

 @GetMapping("/type")
 public List<AdvertisementResponse> searchByType(
         @RequestParam SearchType value,
         @RequestParam(defaultValue = "50") long limit,
         @RequestParam(defaultValue = "0") long offset,
         @RequestHeader(value = "X-User-Id", required = false) UUID userId) { // Добавлен userId
  return searchService.searchByType(value, limit, offset, userId);
 }

 @GetMapping("/category")
 public List<AdvertisementResponse> searchByCategory(
         @RequestParam String value,
         @RequestParam(defaultValue = "50") long limit,
         @RequestParam(defaultValue = "0") long offset,
         @RequestHeader(value = "X-User-Id", required = false) UUID userId) { // Добавлен userId
  return searchService.searchByCategory(value, limit, offset, userId);
 }

 // Остальные методы (history, suggestions) остаются без изменений
 @GetMapping("/history")
 public List<SearchHistory> history(@RequestParam UUID userId, @RequestParam(defaultValue = "20") int limit) {
  return searchService.getUserSearchHistory(userId, limit);
 }

 @DeleteMapping("/history/{historyId}")
 public void deleteHistoryEntry(@PathVariable UUID historyId, @RequestParam UUID userId) {
  searchService.deleteSearchHistoryEntry(historyId, userId);
 }

 @DeleteMapping("/history")
 public void clearHistory(@RequestParam UUID userId) {
  searchService.clearUserSearchHistory(userId);
 }

 @GetMapping("/suggestions")
 public List<SearchSuggestion> suggestions(
         @RequestParam(required = false) String prefix,
         @RequestParam(required = false) UUID userId,
         @RequestParam(defaultValue = "10") int limit) {
  return searchService.getSearchSuggestions(prefix, userId, limit);
 }
}