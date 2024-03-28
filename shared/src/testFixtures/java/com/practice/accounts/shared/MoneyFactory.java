package com.practice.accounts.shared;

import java.math.BigDecimal;
import java.util.Currency;

public interface MoneyFactory {

  default Money zeroGPB() {
    return Money.money(BigDecimal.ZERO, Currency.getInstance("GBP"))
        .successfulValue()
        .orElseThrow();
  }

  default Money oneGPB() {
    return Money.money(BigDecimal.ONE, Currency.getInstance("GBP")).successfulValue().orElseThrow();
  }

  default Money twoGPB() {
    return Money.money(BigDecimal.TWO, Currency.getInstance("GBP")).successfulValue().orElseThrow();
  }

  default Money oneUSD() {
    return Money.money(BigDecimal.ONE, Currency.getInstance("USD")).successfulValue().orElseThrow();
  }
}
