package com.mipt.advertisement.model;

import com.mipt.advertisement.model.converter.CategoryConverter;
import jakarta.persistence.*;
import java.util.stream.Collectors;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "advertisements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Advertisement {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Type type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AdvertisementStatus status;

  @Column(name = "author", nullable = false)
  private UUID authorId;

  @Column(nullable = false, length = 255)
  private String name;

  @Column(length = 5000)
  private String description;

  private Long price;

  @Convert(converter = CategoryConverter.class)
  private Category category;

  @Column(name = "is_favorite")
  private boolean isFavorite;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  // Связь с фото
  @OneToMany(mappedBy = "advertisement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @OrderBy("displayOrder ASC")
  @Builder.Default
  private Set<AdvertisementPhoto> photos = new LinkedHashSet<>();


  // Хелпер методы для работы с фото
  public void addPhoto(String photoUrl) {
    AdvertisementPhoto photo = AdvertisementPhoto.builder()
            .advertisement(this)
            .photoUrl(photoUrl)
            .displayOrder(this.photos.size())
            .build();
    this.photos.add(photo);
  }

  public void removePhoto(String photoUrl) {
    this.photos.removeIf(photo -> photo.getPhotoUrl().equals(photoUrl));
    // Переупорядочиваем
    int order = 0;
    for (AdvertisementPhoto photo : this.photos) {
      photo.setDisplayOrder(order++);
    }
  }

  public Set<String> getPhotoUrls() {
    return photos.stream()
            .map(AdvertisementPhoto::getPhotoUrl)
            .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  public void setPhotoUrls(Set<String> photoUrls) {
    this.photos.clear();
    if (photoUrls != null) {
      int order = 0;
      for (String url : photoUrls) {
        this.photos.add(AdvertisementPhoto.builder()
                .advertisement(this)
                .photoUrl(url)
                .displayOrder(order++)
                .build());
      }
    }
  }

  // Валидация (оставляем без изменений)
  public void validateToCreate() {
    validateName();
    validateType();
    validateCategory();
  }

  public void validateToPublish() {
    validateToCreate();
    validatePrice();
    validateDescription();
    validatePhotoUrls();
  }

  private void validateCategory() {
    if (category == null) {
      throw new IllegalArgumentException("Категория обязательна для объявления");
    }
    Type categoryType = Category.getTypeForCategory(category);
    if (categoryType != null && categoryType != this.type) {
      throw new IllegalArgumentException(
              String.format("Категория '%s' не подходит для типа '%s'",
                      category.getDisplayName(), type)
      );
    }
  }

  private void validateName() {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Название объявления не может быть пустым");
    }
    if (name.length() < 3 || name.length() > 250) {
      throw new IllegalArgumentException("Название объявления должно быть от 3 до 255 символов");
    }
  }

  private void validateType() {
    if (type == null) {
      throw new IllegalArgumentException("Тип объявления не может быть пустым");
    }
  }

  private void validatePhotoUrls() {
    if (getPhotoUrls().isEmpty()) {
      throw new IllegalArgumentException("Добавьте хотя бы одно фото");
    }
  }

  private void validateDescription() {
    if (description != null && description.length() > 5000) {
      throw new IllegalArgumentException("Описание не может превышать 5000 символов");
    }
  }

  private void validatePrice() {
    if (price == null) {
      throw new IllegalArgumentException("Цена не может быть пустой");
    }
    if (price <= 0) {
      throw new IllegalArgumentException("Цена должна быть положительной");
    }
  }

  public String getCategoryName() {
    return category != null ? category.name() : null;
  }

  public String getCategoryDisplayName() {
    return category != null ? category.getDisplayName() : null;
  }

  public void toggleFavorite() {
    this.isFavorite = !this.isFavorite;
  }
}