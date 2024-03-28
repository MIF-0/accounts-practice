package com.practice.accounts.shared;

public enum MoneyError {
  NEGATIVE_MONEY("Money can't be negative"),
  WRONG_SUBTRACT_OPERATION("You can't subtract more than you have"),
  WRONG_CURRENCY("Currency should be the same");

  private final String description;

  MoneyError(String description) {
    this.description = description;
  }

  public String description() {
    return description;
  }
}
