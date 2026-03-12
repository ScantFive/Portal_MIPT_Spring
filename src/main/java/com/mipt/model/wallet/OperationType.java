package com.mipt.model.wallet;

public enum OperationType {
  PAY("Оплата"),
  REFUND("Возврат"),
  RESERVE("Резервирование"),
  CANCEL("Отмена резервирования");

  private final String displayName;

  OperationType(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public String toString() {
    return displayName;
  }
}
