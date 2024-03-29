package com.practice.accounts.account.domain;

import java.util.Currency;
import java.util.UUID;

public interface AccountFactory {

  default Account newGBPAccount() {
    return Account.openAccountFor(UUID.randomUUID().toString(), Currency.getInstance("GBP"));
  }

  default Account newUSDAccount() {
    return Account.openAccountFor(UUID.randomUUID().toString(), Currency.getInstance("USD"));
  }
}
