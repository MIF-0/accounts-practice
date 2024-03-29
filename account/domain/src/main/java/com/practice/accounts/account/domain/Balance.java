package com.practice.accounts.account.domain;

import com.practice.accounts.account.domain.error.BalanceError;
import com.practice.accounts.shared.Money;
import com.practice.accounts.shared.MoneyError;
import com.practice.accounts.shared.Result;
import com.practice.accounts.shared.Success;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;
import java.util.StringJoiner;

public class Balance {
  // For now, we can keep as money wrapper,
  // But it quite possible that in future we would need to have it fully separated
  // as balance potentially can be negative and money not
  private final Money value;

  public static Balance emptyBalance(Currency currency) {
    return new Balance(Money.zero(currency));
  }

  public Balance(Money balance) {
    this.value = balance;
  }

  public BigDecimal value() {
    return value.value();
  }

  public Result<Balance, BalanceError> withdraw(Money money) {
    var result = this.value.subtract(money);
    return result.map(newBalance -> new Success<>(new Balance(newBalance)), this::fromMoneyError);
  }

  public Result<Balance, BalanceError> debit(Money money) {
    var result = this.value.add(money);
    return result.map(newBalance -> new Success<>(new Balance(newBalance)), this::fromMoneyError);
  }

  public boolean isNotEqualTo(Object that) {
    return !this.equals(that);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Balance balance)) return false;

    return Objects.equals(value, balance.value);
  }

  @Override
  public int hashCode() {
    return value != null ? value.hashCode() : 0;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Balance.class.getSimpleName() + "[", "]")
        .add("currency=" + value.currency())
        .add("value=" + value.value())
        .toString();
  }

  private BalanceError fromMoneyError(MoneyError moneyError) {
    return switch (moneyError) {
      case NEGATIVE_MONEY, WRONG_SUBTRACT_OPERATION -> BalanceError.NOT_ENOUGH_MONEY;
      case WRONG_CURRENCY -> BalanceError.WRONG_CURRENCY;
    };
  }
}
