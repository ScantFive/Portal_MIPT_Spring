package com.mipt.review.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateReviewRequest {

    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(min = 10, max = 5000, message = "Комментарий от 10 до 5000 символов")
    private String comment;

    private Boolean isAnonymous;  // Можно изменить анонимность
}