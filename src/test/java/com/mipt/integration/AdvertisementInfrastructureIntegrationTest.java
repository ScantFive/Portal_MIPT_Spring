package com.mipt.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mipt.advertisement.model.Advertisement;
import com.mipt.advertisement.model.AdvertisementStatus;
import com.mipt.advertisement.model.Category;
import com.mipt.advertisement.model.Type;
import com.mipt.advertisement.repository.AdvertisementJpaRepository;
import com.mipt.advertisement.service.AdvertisementService;
import com.mipt.advertisement.service.KafkaEventPublisher;
import com.mipt.config.advertisement.KafkaTopicConfig;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = AdvertisementInfrastructureIntegrationTest.TestConfig.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/portal_DB",
    "spring.datasource.username=postgres",
    "spring.datasource.password=JavaMTS",
    "spring.kafka.bootstrap-servers=localhost:9092",
    "kafka.topic.advertisement-events=advertisement-events"
})
class AdvertisementInfrastructureIntegrationTest {

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @EntityScan(basePackageClasses = Advertisement.class)
  @EnableJpaRepositories(basePackageClasses = AdvertisementJpaRepository.class)
  @ComponentScan(basePackageClasses = {
      AdvertisementService.class,
      KafkaEventPublisher.class,
      KafkaTopicConfig.class
  })
  static class TestConfig {
  }

  private static final String TOPIC = "advertisement-events";

  @Autowired
  private AdvertisementService advertisementService;

  private UUID createdId;

  @AfterEach
  void cleanup() {
    if (createdId != null) {
      try {
        advertisementService.delete(createdId);
      } catch (Exception ignored) {
        // Ignore cleanup failures to keep assertion signal from test body.
      }
    }
  }

  @Test
  void createShouldPersistToDbAndPublishKafkaEvent() {
    KafkaConsumer<String, byte[]> consumer = createConsumer();
    consumer.subscribe(Set.of(TOPIC));
    consumer.poll(Duration.ofSeconds(1));

    Advertisement ad = new Advertisement();
    ad.setId(UUID.randomUUID());
    ad.setType(Type.SERVICES);
    ad.setStatus(AdvertisementStatus.DRAFT);
    ad.setAuthorId(UUID.randomUUID());
    ad.setName("Integration Kafka DB Test");
    ad.setDescription("checks persistence and kafka");
    ad.setCategory(Category.УСЛУГИ_КОНСУЛЬТАЦИИ_ПРОГРАММИРОВАНИЕ);
    ad.setCreatedAt(Instant.now());
    ad.setPhotoUrls(new HashSet<>());

    Advertisement saved = advertisementService.create(ad);
    createdId = saved.getId();

    Optional<Advertisement> fromDb = advertisementService.findById(saved.getId());
    assertTrue(fromDb.isPresent(), "Advertisement must be persisted in DB");
    assertNotNull(fromDb.get().getId());

    boolean eventObserved = false;
    long deadline = System.currentTimeMillis() + 10_000;
    while (System.currentTimeMillis() < deadline && !eventObserved) {
      ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));
      for (ConsumerRecord<String, byte[]> record : records) {
        String payload = new String(record.value(), StandardCharsets.UTF_8);
        if (payload.contains("ADVERTISEMENT_CREATED") && payload.contains(saved.getId().toString())) {
          eventObserved = true;
          break;
        }
      }
    }
    consumer.close();

    assertTrue(eventObserved, "Kafka event ADVERTISEMENT_CREATED must be published");
  }

  private KafkaConsumer<String, byte[]> createConsumer() {
    Properties props = new Properties();
    props.put("bootstrap.servers", "localhost:9092");
    props.put("group.id", "it-advertisement-" + UUID.randomUUID());
    props.put("auto.offset.reset", "earliest");
    props.put("enable.auto.commit", "true");
    props.put("key.deserializer", StringDeserializer.class.getName());
    props.put("value.deserializer", ByteArrayDeserializer.class.getName());
    return new KafkaConsumer<>(props);
  }
}
