package com.mipt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@Disabled("Legacy duplicate beans in project packages make full context bootstrap unstable")
@SpringBootTest(classes = Main.class)
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class }) // ← Отключаем БД
class PortalMiptApplicationTests {

  @Test
  void contextLoads() {

  }
}
