package com.practice.accounts.application.configuration;

import com.practice.accounts.account.domain.Account;
import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.Money;
import java.math.BigDecimal;
import java.util.Currency;

public class Limitation {
  private final Currency supportedCurrency;
  private final Account companyAccount;
  private final AccountId companyAccountId;

  public Limitation() {
    this.supportedCurrency = Currency.getInstance("GBP");
    var companyAccount = Account.openAccountFor("COMPANY_GBP", supportedCurrency);
    var money =
        Money.money(BigDecimal.valueOf(1_000_000), this.supportedCurrency)
            .successfulValue()
            .orElseThrow();
    this.companyAccount = companyAccount.debit(money).successfulValue().orElseThrow();
    this.companyAccountId = companyAccount.id();
  }

  public Currency supportedCurrency() {
    return supportedCurrency;
  }

  public Account initialCompanyAccount() {
    return companyAccount;
  }

  public AccountId companyAccountId() {
    return companyAccountId;
  }
}
