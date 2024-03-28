package com.practice.accounts.account.domain;

public enum BalanceError {
  NOT_ENOUGH_MONEY("You have not enough money to perform this operation"),
  WRONG_CURRENCY("Your operation should have same currency as your balance");

  private final String description;

  BalanceError(String description) {
    this.description = description;
  }

  public String description() {
    return description;
  }
}
