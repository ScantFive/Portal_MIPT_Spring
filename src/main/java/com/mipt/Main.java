package com.mipt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {
    "com.mipt.advertisement.model",
    "com.mipt.chat.model",
    "com.mipt.mainpage.model",
    "com.mipt.search.model",
    "com.mipt.user.model",
    "com.mipt.wallet.model"
})
public class Main {

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }
}
