package com.mipt.controller;

import com.mipt.advertisement.model.Advertisement;
import com.mipt.advertisement.model.AdvertisementStatus;
import com.mipt.advertisement.model.Category;
import com.mipt.advertisement.service.AdvertisementService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/advertisements")
@RequiredArgsConstructor
public class AdvertisementController {

 private final AdvertisementService advertisementService;

 @GetMapping("/{id}")
 public Advertisement getById(@PathVariable UUID id) {
  return advertisementService
    .findById(id)
    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Advertisement not found"));
 }

 @GetMapping
 public List<Advertisement> list(
   @RequestParam(required = false) UUID authorId,
   @RequestParam(required = false) AdvertisementStatus status,
   @RequestParam(required = false) Category category,
   @RequestParam(required = false, defaultValue = "false") boolean favoritesOnly) {
  if (favoritesOnly) {
   return advertisementService.findFavorites();
  }
  if (authorId != null) {
   return advertisementService.findByAuthorId(authorId);
  }
  if (status != null) {
   return advertisementService.findByStatus(status);
  }
  if (category != null) {
   return advertisementService.findByCategory(category);
  }
  return advertisementService.findByStatus(AdvertisementStatus.ACTIVE);
 }

 @PostMapping
 @ResponseStatus(HttpStatus.CREATED)
 public Advertisement create(@RequestBody Advertisement advertisement) {
  return advertisementService.create(advertisement);
 }

 @PutMapping("/{id}")
 public Advertisement update(@PathVariable UUID id, @RequestBody Advertisement advertisement) {
  advertisement.setId(id);
  return advertisementService.update(advertisement);
 }

 @PostMapping("/{id}/publish")
 public Advertisement publish(@PathVariable UUID id) {
  Advertisement advertisement = getById(id);
  return advertisementService.publish(advertisement);
 }

 @PostMapping("/{id}/pause")
 public Advertisement pause(@PathVariable UUID id) {
  Advertisement advertisement = getById(id);
  return advertisementService.pause(advertisement);
 }

 @PostMapping("/{id}/favorite")
 public Advertisement setFavorite(@PathVariable UUID id, @RequestParam boolean value) {
  return advertisementService.setFavorite(id, value);
 }

 @DeleteMapping("/{id}")
 @ResponseStatus(HttpStatus.NO_CONTENT)
 public void delete(@PathVariable UUID id) {
  advertisementService.delete(id);
 }
}
