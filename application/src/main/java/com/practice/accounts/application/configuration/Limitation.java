package com.practice.accounts.application.configuration;

import com.practice.accounts.account.domain.Account;
import com.practice.accounts.shared.Money;
import java.math.BigDecimal;
import java.util.Currency;

public class Limitation {
  private final Currency supportedCurrency;
  private final Account companyAccount;

  public Limitation() {
    this.supportedCurrency = Currency.getInstance("GBP");
    var companyAccount = Account.openAccountFor("COMPANY_GBP", supportedCurrency);
    var money =
        Money.money(BigDecimal.valueOf(1_000_000), this.supportedCurrency)
            .successfulValue()
            .orElseThrow();
    this.companyAccount = companyAccount.debit(money).successfulValue().orElseThrow();
  }

  public Currency supportedCurrency() {
    return supportedCurrency;
  }

  public Account companyAccount() {
    return companyAccount;
  }
}
