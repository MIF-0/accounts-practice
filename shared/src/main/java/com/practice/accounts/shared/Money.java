package com.practice.accounts.shared;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;

public class Money {
  private final BigDecimal value;
  private final Currency currency;

  public static Result<Money, MoneyError> money(BigDecimal value, Currency currency) {
    if (value.compareTo(BigDecimal.ZERO) < 0) {
      return new Failed<>(MoneyError.NEGATIVE_MONEY, Optional.of("Was: " + value));
    }
    return new Success<>(new Money(value, currency));
  }

  public static Money zero(Currency currency) {
    return new Money(BigDecimal.ZERO, currency);
  }

  private Money(BigDecimal value, Currency currency) {
    this.value = value;
    this.currency = currency;
  }

  public BigDecimal value() {
    return value;
  }

  public Currency currency() {
    return currency;
  }

  public Result<Money, MoneyError> add(Money that) {
    var validationResult = validate(that);
    return validationResult.map(success -> processAddition(that), Function.identity());
  }

  public Result<Money, MoneyError> subtract(Money that) {
    var validationResult = validate(that);
    return validationResult.map(success -> processSubtraction(that), Function.identity());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Money money)) return false;

    if (!Objects.equals(value, money.value)) return false;
    return Objects.equals(currency, money.currency);
  }

  @Override
  public int hashCode() {
    int result = value != null ? value.hashCode() : 0;
    result = 31 * result + (currency != null ? currency.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Money.class.getSimpleName() + "[", "]")
        .add("value=" + value)
        .add("currency=" + currency)
        .toString();
  }

  private Result<Money, MoneyError> processAddition(Money that) {
    var newValue = this.value.add(that.value);
    return new Success<>(new Money(newValue, this.currency));
  }

  private Result<Money, MoneyError> processSubtraction(Money that) {
    var newValue = this.value.subtract(that.value);
    return Money.money(newValue, this.currency).map(Success::new, failure -> failure);
  }

  private Result<Void, MoneyError> validate(Money that) {
    if (!this.currency.equals(that.currency)) {
      return new Failed<>(
          MoneyError.WRONG_CURRENCY,
          Optional.of("Was: " + this.currency + " and " + that.currency));
    }
    return new Success<>(null);
  }
}
