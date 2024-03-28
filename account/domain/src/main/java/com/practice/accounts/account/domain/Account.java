package com.practice.accounts.account.domain;

import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.Money;
import com.practice.accounts.shared.Result;
import com.practice.accounts.shared.Success;
import java.util.Currency;
import java.util.function.Function;

public class Account {
  private final AccountId accountId;
  private final Balance balance;
  private final int version;

  public static Account openAccountFor(Currency currency) {
    return new Account(AccountId.generate(), Balance.emptyBalance(currency), 0);
  }

  private Account(AccountId accountId, Balance balance, int version) {
    this.accountId = accountId;
    this.balance = balance;
    this.version = version;
  }

  public Result<Account, BalanceError> withdraw(Money money) {
    var result = balance.withdraw(money);
    var newVersion = this.version + 1;
    return result.map(
        balance -> new Success<>(new Account(accountId, balance, newVersion)), Function.identity());
  }

  public Result<Account, BalanceError> debit(Money money) {
    var result = balance.debit(money);
    var newVersion = this.version + 1;
    return result.map(
        balance -> new Success<>(new Account(accountId, balance, newVersion)), Function.identity());
  }
}
