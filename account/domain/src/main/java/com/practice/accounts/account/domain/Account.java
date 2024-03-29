package com.practice.accounts.account.domain;

import com.practice.accounts.account.domain.error.BalanceError;
import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.Money;
import com.practice.accounts.shared.Result;
import com.practice.accounts.shared.Success;
import com.practice.accounts.shared.Version;
import java.util.Currency;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;

public class Account {
  private final AccountId id;
  private final String name;
  private final Balance balance;
  private final Version version;

  public static Account openAccountFor(String name, Currency currency) {
    return new Account(
        AccountId.generate(), name, Balance.emptyBalance(currency), Version.createFirstVersion());
  }

  private Account(AccountId id, String name, Balance balance, Version version) {
    this.id = id;
    this.name = name;
    this.balance = balance;
    this.version = version;
  }

  public AccountId id() {
    return id;
  }

  public Balance balance() {
    return balance;
  }

  public Version version() {
    return version;
  }

  public Result<Account, BalanceError> withdraw(Money money) {
    var result = balance.withdraw(money);
    var newVersion = this.version.next();
    return result.map(
        balance -> new Success<>(new Account(id, name, balance, newVersion)), Function.identity());
  }

  public Result<Account, BalanceError> debit(Money money) {
    var result = balance.debit(money);
    var newVersion = this.version.next();
    return result.map(
        balance -> new Success<>(new Account(id, name, balance, newVersion)), Function.identity());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Account account)) return false;

    return Objects.equals(id, account.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Account.class.getSimpleName() + "[", "]")
        .add("accountId=" + id)
        .add("name='" + name + "'")
        .add("balance=" + balance)
        .add("version=" + version)
        .toString();
  }
}
