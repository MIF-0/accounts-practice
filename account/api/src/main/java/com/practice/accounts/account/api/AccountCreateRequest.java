package com.practice.accounts.account.api;

import com.practice.accounts.shared.RequestId;
import java.util.Currency;

public final class AccountCreateRequest extends Request {
  private final String name;
  private final Currency currency;

  public AccountCreateRequest(RequestId requestId, String name, Currency currency) {
    super(requestId);
    this.name = name;
    this.currency = currency;
  }

  public String name() {
    return name;
  }

  public Currency currency() {
    return currency;
  }
}
