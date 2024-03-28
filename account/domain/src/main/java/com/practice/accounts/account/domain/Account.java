package com.practice.accounts.account.domain;

import com.practice.accounts.account.domain.error.BalanceError;
import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.Money;
import com.practice.accounts.shared.Result;
import com.practice.accounts.shared.Success;
import java.util.Currency;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;

public class Account {
  private final AccountId accountId;
  private final String name;
  private final Balance balance;
  private final int version;

  public static Account openAccountFor(String name, Currency currency) {
    return new Account(AccountId.generate(), name, Balance.emptyBalance(currency), 0);
  }

  private Account(AccountId accountId, String name, Balance balance, int version) {
    this.accountId = accountId;
    this.name = name;
    this.balance = balance;
    this.version = version;
  }

  public AccountId accountId() {
    return accountId;
  }

  public Result<Account, BalanceError> withdraw(Money money) {
    var result = balance.withdraw(money);
    var newVersion = this.version + 1;
    return result.map(
        balance -> new Success<>(new Account(accountId, name, balance, newVersion)),
        Function.identity());
  }

  public Result<Account, BalanceError> debit(Money money) {
    var result = balance.debit(money);
    var newVersion = this.version + 1;
    return result.map(
        balance -> new Success<>(new Account(accountId, name, balance, newVersion)),
        Function.identity());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Account account)) return false;

    return Objects.equals(accountId, account.accountId);
  }

  @Override
  public int hashCode() {
    return accountId != null ? accountId.hashCode() : 0;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Account.class.getSimpleName() + "[", "]")
        .add("accountId=" + accountId)
        .add("name='" + name + "'")
        .add("balance=" + balance)
        .add("version=" + version)
        .toString();
  }
}
