package com.mipt.model.advertisement;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Advertisement {

    private UUID id;
    private Type type;
    private AdvertisementStatus status;
    private UUID authorId;
    private String name;
    private String description;
    private Long price;
    private Set<String> photoUrls;
    private Category category; // Теперь одна категория вместо списка
    private boolean isFavorite;
    private Instant createdAt;

    public Advertisement(UUID uuid, Type type, UUID authorId, String name, String description,
        Instant createdAt) {
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
                    category.getDisplayName(), type)
            );
        }
    }

    private void validateName() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название объявления не может быть пустым");
        }
        if (name.length() < 3 || name.length() > 250) {
            throw new IllegalArgumentException(
                "Название объявления должно быть от 3 до 255 символов");
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