package com.mipt.wallet.controller.dto;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationRequest {
    private UUID clientId;
    private UUID performerId;
    private Long amount;
    private String title;
}
