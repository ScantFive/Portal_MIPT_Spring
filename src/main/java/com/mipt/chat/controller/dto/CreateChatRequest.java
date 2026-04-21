package com.mipt.chat.controller.dto;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatRequest {
    private UUID ownerId;
    private UUID memberId;
}
