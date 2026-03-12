package com.mipt;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootTest
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})  // ← Отключаем БД
class PortalMiptApplicationTests {

  @Test
  void contextLoads() {

  }
}
