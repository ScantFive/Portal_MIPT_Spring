package com.mipt.chat.service;

import com.mipt.chat.event.ChatEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatKafkaEventPublisher {

 private final KafkaTemplate<String, Object> kafkaTemplate;

 @Value("${kafka.topic.chat-events:chat-events}")
 private String topic;

 public void publish(ChatEvent event) {
  if (event == null || event.getChatId() == null) {
   return;
  }

  try {
   kafkaTemplate.send(topic, event.getChatId().toString(), event);
   log.debug("Kafka CHAT event published: {} for chat {}", event.getEventType(), event.getChatId());
  } catch (Exception ex) {
   log.error("Kafka CHAT event publish failed: {}", event.getEventType(), ex);
  }
 }
}
