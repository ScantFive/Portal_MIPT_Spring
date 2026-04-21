package com.mipt.chat.controller;

import com.mipt.chat.controller.dto.CreateChatRequest;
import com.mipt.chat.controller.dto.SendMessageRequest;
import com.mipt.chat.event.ChatEvent;
import com.mipt.chat.model.Chat;
import com.mipt.chat.model.Message;
import com.mipt.chat.repository.ChatJpaRepository;
import com.mipt.chat.repository.MessageJpaRepository;
import com.mipt.chat.service.ChatKafkaEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatJpaRepository chatRepository;
    private final MessageJpaRepository messageRepository;
    private final ChatKafkaEventPublisher eventPublisher;

    @GetMapping
    public ResponseEntity<List<Chat>> getChats(@RequestParam UUID userId) {
        log.debug("GET /chats?userId={}", userId);
        List<Chat> chats = chatRepository.findAll().stream()
                .filter(c -> c.getOwnerId().equals(userId) || c.getMemberId().equals(userId))
                .sorted(Comparator.comparing(Chat::getLastUpdate).reversed())
                .toList();
        return ResponseEntity.ok(chats);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Chat> createChat(@RequestBody CreateChatRequest req) {
        log.debug("POST /chats owner={} member={}", req.getOwnerId(), req.getMemberId());
        if (req.getOwnerId() == null || req.getMemberId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ownerId и memberId обязательны");
        }
        if (req.getOwnerId().equals(req.getMemberId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя создать чат с самим собой");
        }
        // Return existing chat if already present (both directions)
        var existing = chatRepository.findByOwnerIdAndMemberId(req.getOwnerId(), req.getMemberId())
                .or(() -> chatRepository.findByOwnerIdAndMemberId(req.getMemberId(), req.getOwnerId()));
        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get());
        }
        Chat chat = Chat.builder()
                .ownerId(req.getOwnerId())
                .memberId(req.getMemberId())
                .build();
        Chat saved = chatRepository.save(chat);
        publishEvent(ChatEvent.chatCreated(saved));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable UUID chatId) {
        log.debug("GET /chats/{}/messages", chatId);
        if (!chatRepository.existsById(chatId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Чат не найден: " + chatId);
        }
        List<Message> messages = messageRepository.findByChatId(chatId).stream()
                .sorted(Comparator.comparing(Message::getSendingTime))
                .toList();
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{chatId}/messages")
    @Transactional
    public ResponseEntity<Message> sendMessage(@PathVariable UUID chatId,
                                               @RequestBody SendMessageRequest req) {
        log.debug("POST /chats/{}/messages sender={}", chatId, req.getSenderId());
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Чат не найден: " + chatId));
        if (req.getText() == null || req.getText().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Сообщение не может быть пустым");
        }
        Message message = Message.builder()
                .chatId(chatId)
                .senderId(req.getSenderId())
                .text(req.getText().trim())
                .build();
        Message saved = messageRepository.save(message);
        chat.setLastUpdate(saved.getSendingTime());
        chatRepository.save(chat);
        UUID receiverId = req.getSenderId() != null && req.getSenderId().equals(chat.getOwnerId())
                ? chat.getMemberId()
                : chat.getOwnerId();
        publishEvent(ChatEvent.messageSent(saved, receiverId));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    private void publishEvent(ChatEvent event) {
        try {
            eventPublisher.publish(event);
        } catch (Exception ex) {
            log.error("Unable to publish CHAT event {}", event != null ? event.getEventType() : "unknown", ex);
        }
    }
}
