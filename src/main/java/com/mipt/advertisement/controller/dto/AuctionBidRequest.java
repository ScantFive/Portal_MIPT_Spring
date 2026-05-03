package com.mipt.advertisement.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.UUID;

@Data
public class AuctionBidRequest {

    @NotNull(message = "bidderId обязателен")
    private UUID bidderId;

    @NotNull(message = "Сумма ставки обязательна")
    @Positive(message = "Ставка должна быть положительной")
    private Long amount;
}
