package com.practice.accounts.account.api;

import com.practice.accounts.account.domain.Account;

public class AccountInfo {
  private final Account account;

  public AccountInfo(Account account) {
    this.account = account;
  }

  @Override
  public String toString() {
    return account.toString();
  }
}
