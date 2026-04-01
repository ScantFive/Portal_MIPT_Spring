package com.mipt.wallet.service;

import com.mipt.wallet.event.WalletEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletKafkaEventPublisher {

 private final KafkaTemplate<String, Object> kafkaTemplate;

 @Value("${kafka.topic.wallet-events:wallet-events}")
 private String topic;

 public void publish(WalletEvent event) {
  if (event == null) {
   return;
  }

  String key = event.getWalletOwnerId() != null ? event.getWalletOwnerId().toString() : "wallet";
  try {
   kafkaTemplate.send(topic, key, event);
   log.debug("Kafka WALLET event published: {} key={}", event.getEventType(), key);
  } catch (Exception ex) {
   log.error("Kafka WALLET event publish failed: {}", event.getEventType(), ex);
  }
 }
}
