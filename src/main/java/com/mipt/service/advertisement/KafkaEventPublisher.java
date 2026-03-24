package com.mipt.service.advertisement;

import com.mipt.event.AdvertisementEvent;
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
        log.info("Publishing event: {} for advertisement: {}",
                event.getEventType(), event.getAdvertisementId());

        try {
            kafkaTemplate.send(topic, event.getAdvertisementId().toString(), event);
            log.debug("Event published successfully to topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to publish event: {}", event.getEventType(), e);
        }
    }
}