package com.mipt.advertisement.model;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Price {
  BigDecimal price;
  UnitOfTime unitOfTime;

  public Price(BigDecimal price, UnitOfTime unitOfTime) {
    this.price = price;
    this.unitOfTime = unitOfTime;
  }
}
