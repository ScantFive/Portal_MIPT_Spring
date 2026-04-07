package com.mipt.review.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateReviewRequest {

    private UUID advertisementId;  // null = отзыв на продавца (без конкретного объявления)

    @NotNull(message = "ID продавца обязателен")
    private UUID sellerId;

    @NotNull(message = "Оценка обязательна")
    @Min(1)
    @Max(5)
    private Integer rating;

    @NotBlank(message = "Комментарий не может быть пустым")
    @Size(min = 10, max = 5000, message = "Комментарий от 10 до 5000 символов")
    private String comment;
    private boolean isAnonymous;
}