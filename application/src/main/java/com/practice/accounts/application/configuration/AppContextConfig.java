package com.practice.accounts.application.configuration;

public class AppContextConfig {
  private final AccountContextConfig accountContextConfig;
  private final TransfersContextConfig transfersContextConfig;
  private final Limitation limitation;

  public AppContextConfig() {
    this.accountContextConfig = new AccountContextConfig();
    this.limitation = new Limitation();
    this.transfersContextConfig =
        new TransfersContextConfig(accountContextConfig.accounts(), limitation);
  }

  public AccountContextConfig accountContextConfig() {
    return accountContextConfig;
  }

  public Limitation limitation() {
    return limitation;
  }

  public TransfersContextConfig transfersContextConfig() {
    return transfersContextConfig;
  }
}
