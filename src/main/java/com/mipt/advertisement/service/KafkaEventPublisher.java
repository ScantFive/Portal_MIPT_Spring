package com.mipt.advertisement.service;

import com.mipt.advertisement.event.AdvertisementEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventPublisher {

  private final KafkaTemplate<String, AdvertisementEvent> kafkaTemplate;

  @Value("${kafka.topic.advertisement-events:advertisement-events}")
  private String topic;

  public void publishEvent(AdvertisementEvent event) {
    if (event == null || event.getAdvertisementId() == null) {
      return;
    }

    try {
      kafkaTemplate.send(topic, event.getAdvertisementId().toString(), event);
      log.debug("Kafka event published: {} for ad {}", event.getEventType(), event.getAdvertisementId());
    } catch (Exception ex) {
      // Kafka publication must not break main business flow.
      log.error("Kafka publish failed for event {}", event.getEventType(), ex);
    }
  }
}
