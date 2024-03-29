package com.practice.accounts.application.configuration;

public class AppContextConfig {
  private final AccountContextConfig accountContextConfig;
  private final Limitation limitation;

  public AppContextConfig() {
    this.accountContextConfig = new AccountContextConfig();
    this.limitation = new Limitation();
  }

  public AccountContextConfig accountContextConfig() {
    return accountContextConfig;
  }

  public Limitation limitation() {
    return limitation;
  }
}
