package com.mipt.controller.dto;

import com.mipt.model.advertisement.Type;
import jakarta.validation.constraints.*;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import lombok.Data;

@Data
public class AdvertisementRequest {

    @NotNull(message = "Тип объявления обязателен")
    private Type type;

    @NotNull(message = "ID автора обязателен")
    private UUID authorId;

    @NotBlank(message = "Название не может быть пустым")
    @Size(min = 3, max = 255, message = "Название должно быть от 3 до 255 символов")
    private String name;

    @Size(max = 5000, message = "Описание не может превышать 5000 символов")
    private String description;

    @Positive(message = "Цена должна быть положительной")
    private Long price;

    @NotNull(message = "Категория обязательна")
    private String category; // Можно передавать как displayName или enum name

    private Set<String> photoUrls = new TreeSet<>();
}