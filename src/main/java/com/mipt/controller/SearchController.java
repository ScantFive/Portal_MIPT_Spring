package com.mipt.controller;

import com.mipt.mainpage.model.ShortAdvert;
import com.mipt.search.model.SearchHistory;
import com.mipt.search.model.SearchQuery;
import com.mipt.search.model.SearchSuggestion;
import com.mipt.search.model.SearchType;
import com.mipt.search.service.SearchService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

 private final SearchService searchService;

 @PostMapping
 public List<ShortAdvert> search(
   @RequestBody(required = false) SearchQuery query,
   @RequestParam(defaultValue = "50") long limit,
   @RequestParam(defaultValue = "0") long offset,
   @RequestParam(required = false) UUID userId) {
  if (userId == null) {
   return searchService.search(limit, offset, query);
  }
  return searchService.search(limit, offset, query, userId);
 }

 @GetMapping("/text")
 public List<ShortAdvert> searchByText(
   @RequestParam String value,
   @RequestParam(defaultValue = "50") long limit,
   @RequestParam(defaultValue = "0") long offset) {
  return searchService.searchByText(value, limit, offset);
 }

 @GetMapping("/type")
 public List<ShortAdvert> searchByType(
   @RequestParam SearchType value,
   @RequestParam(defaultValue = "50") long limit,
   @RequestParam(defaultValue = "0") long offset) {
  return searchService.searchByType(value, limit, offset);
 }

 @GetMapping("/category")
 public List<ShortAdvert> searchByCategory(
   @RequestParam String value,
   @RequestParam(defaultValue = "50") long limit,
   @RequestParam(defaultValue = "0") long offset) {
  return searchService.searchByCategory(value, limit, offset);
 }

 @GetMapping("/history")
 public List<SearchHistory> history(
   @RequestParam UUID userId,
   @RequestParam(defaultValue = "20") int limit) {
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
