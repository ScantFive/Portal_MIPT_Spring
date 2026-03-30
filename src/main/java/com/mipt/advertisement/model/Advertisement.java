package com.mipt.advertisement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "advertisements")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Advertisement {

  @Id
  @Column(name = "id")
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private Type type;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private AdvertisementStatus status;

  @Column(name = "author", nullable = false)
  private UUID authorId;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Column(name = "description", length = 5000)
  private String description;

  @Column(name = "price")
  private Long price;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "advertisement_photos", joinColumns = @JoinColumn(name = "advertisement_id"))
  @Column(name = "photo_url")
  @OrderBy("display_order ASC")
  private Set<String> photoUrls;

  @Enumerated(EnumType.STRING)
  @Column(name = "category")
  private Category category;

  @Column(name = "is_favorite", nullable = false)
  private boolean isFavorite;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public Advertisement(UUID uuid, Type type, UUID authorId, String name, String description, Instant createdAt) {
    this.id = uuid;
    this.type = type;
    status = AdvertisementStatus.DRAFT;
    this.authorId = authorId;
    this.name = name;
    this.description = description;
    this.price = null;
    this.photoUrls = new TreeSet<>();
    this.category = null; // Должна быть установлена позже
    this.isFavorite = false;
    this.createdAt = createdAt;
  }

  // Методы валидации
  public void validateToCreate() {
    validateName();
    validateType();
    validateCategory(); // Категория обязательна даже при создании
  }

  public void validateToPublish() {
    validateToCreate();
    validatePrice();
    validateDescription();
    validateCategory(); // Убеждаемся, что категория установлена
    validatePhotoUrls();
  }

  private void validateCategory() {
    if (category == null) {
      throw new IllegalArgumentException("Категория обязательна для объявления");
    }

    // Проверяем, что категория соответствует типу
    Type categoryType = Category.getTypeForCategory(category);
    if (categoryType != null && categoryType != this.type) {
      throw new IllegalArgumentException(
          String.format("Категория '%s' не подходит для типа '%s'. " +
              "Для товаров выберите категорию из раздела 'товары', " +
              "для услуг - из раздела 'услуги'",
              category.getDisplayName(), type));
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
    if (photoUrls.isEmpty()) {
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

  // Геттеры для удобства
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
