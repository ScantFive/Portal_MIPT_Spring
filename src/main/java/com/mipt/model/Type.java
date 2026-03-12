package com.mipt.model;

import lombok.Getter;

@Getter
public enum Type {
  OBJECTS("Барахолка"),
  SERVICES("Услуги");
  private final String value;

  Type(String value) {
    this.value = value;
  }

}
