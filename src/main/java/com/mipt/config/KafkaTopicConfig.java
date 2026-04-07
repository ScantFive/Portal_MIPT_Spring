package com.mipt.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

  @Value("${kafka.topic.advertisement-events:advertisement-events}")
  private String advertisementEventsTopic;

  @Value("${kafka.topic.user-events:user-events}")
  private String userEventsTopic;

  @Value("${kafka.topic.chat-events:chat-events}")
  private String chatEventsTopic;

  @Value("${kafka.topic.favorite-events:favorite-events}")
  private String favoriteEventsTopic;

  @Value("${kafka.topic.search-history-events:search-history-events}")
  private String searchHistoryEventsTopic;

  @Value("${kafka.topic.wallet-events:wallet-events}")
  private String walletEventsTopic;

  @Value("${kafka.topic.mainpage-events:mainpage-events}")
  private String mainpageEventsTopic;

  @Bean
  public NewTopic advertisementEventsTopic() {
    return TopicBuilder.name(advertisementEventsTopic)
        .partitions(3)
        .replicas(1)
        .config("retention.ms", "604800000")
        .config("compression.type", "snappy")
        .build();
  }

  @Bean
  public NewTopic userEventsTopic() {
    return TopicBuilder.name(userEventsTopic)
        .partitions(3)
        .replicas(1)
        .config("retention.ms", "604800000")
        .config("compression.type", "snappy")
        .build();
  }

  @Bean
  public NewTopic chatEventsTopic() {
    return TopicBuilder.name(chatEventsTopic)
        .partitions(3)
        .replicas(1)
        .config("retention.ms", "259200000")
        .config("compression.type", "snappy")
        .build();
  }

  @Bean
  public NewTopic favoriteEventsTopic() {
    return TopicBuilder.name(favoriteEventsTopic)
        .partitions(3)
        .replicas(1)
        .config("retention.ms", "604800000")
        .config("compression.type", "snappy")
        .build();
  }

  @Bean
  public NewTopic searchHistoryEventsTopic() {
    return TopicBuilder.name(searchHistoryEventsTopic)
        .partitions(3)
        .replicas(1)
        .config("retention.ms", "2592000000")
        .config("compression.type", "snappy")
        .build();
  }

  @Bean
  public NewTopic walletEventsTopic() {
    return TopicBuilder.name(walletEventsTopic)
        .partitions(3)
        .replicas(1)
        .config("retention.ms", "1209600000")
        .config("compression.type", "snappy")
        .build();
  }

  @Bean
  public NewTopic mainpageEventsTopic() {
    return TopicBuilder.name(mainpageEventsTopic)
        .partitions(3)
        .replicas(1)
        .config("retention.ms", "604800000")
        .config("compression.type", "snappy")
        .build();
  }
}
