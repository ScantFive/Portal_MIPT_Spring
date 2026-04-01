package com.mipt.search.service;

import com.mipt.search.event.SearchHistoryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchKafkaEventPublisher {

 private final KafkaTemplate<String, Object> kafkaTemplate;

 @Value("${kafka.topic.search-history-events:search-history-events}")
 private String topic;

 public void publish(SearchHistoryEvent event) {
  if (event == null || event.getUserId() == null) {
   return;
  }

  try {
   kafkaTemplate.send(topic, event.getUserId().toString(), event);
   log.debug("Kafka SEARCH event published: {} for user {}", event.getEventType(), event.getUserId());
  } catch (Exception ex) {
   log.error("Kafka SEARCH event publish failed: {}", event.getEventType(), ex);
  }
 }
}
