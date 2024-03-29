package com.practice.accounts.application.configuration;

import com.practice.accounts.account.api.Accounts;
import com.practice.accounts.account.domain.AccountStorage;
import com.practice.accounts.account.external.AccountRepository;

public class AccountContextConfig {
  private final AccountStorage accountStorage;
  private final Accounts accounts;

  public AccountContextConfig() {
    this.accountStorage = new AccountRepository();
    this.accounts = new Accounts(this.accountStorage);
  }

  public AccountStorage accountStorage() {
    return accountStorage;
  }

  public Accounts accounts() {
    return accounts;
  }
}
