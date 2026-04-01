package com.mipt.mainpage.service;

import com.mipt.mainpage.event.MainPageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainPageKafkaEventPublisher {

 private final KafkaTemplate<String, Object> kafkaTemplate;

 @Value("${kafka.topic.mainpage-events:mainpage-events}")
 private String topic;

 public void publish(MainPageEvent event) {
  if (event == null || event.getUserId() == null) {
   return;
  }

  try {
   kafkaTemplate.send(topic, event.getUserId().toString(), event);
   log.debug("Kafka MAINPAGE event published: {} for user {}", event.getEventType(), event.getUserId());
  } catch (Exception ex) {
   log.error("Kafka MAINPAGE event publish failed: {}", event.getEventType(), ex);
  }
 }
}
