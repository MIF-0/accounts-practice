package com.practice.accounts.account.api;

import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.Money;
import com.practice.accounts.shared.RequestId;

public final class DebitRequest extends Request {
  private final AccountId accountId;
  private final Money money;

  public DebitRequest(RequestId requestId, AccountId accountId, Money money) {
    super(requestId);
    this.accountId = accountId;
    this.money = money;
  }

  public AccountId accountId() {
    return accountId;
  }

  public Money money() {
    return money;
  }
}
