package com.mipt.model.advertisement;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

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
