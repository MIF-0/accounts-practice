package com.practice.accounts.account.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.practice.accounts.shared.MoneyFactory;
import java.util.Currency;
import org.junit.jupiter.api.Test;

public class AccountTest implements MoneyFactory {

  @Test
  public void shouldReturnFailureIfCurrencyDifferentForDebit() {
    // GIVEN
    var balance = Account.openAccountFor("Name", Currency.getInstance("GBP"));
    var addition = oneUSD();
    // WHEN
    var result = balance.debit(addition);

    // THEN
    assertThat(result.isFailure()).isTrue();
  }

  @Test
  public void shouldSuccessfullyDebitMoney() {
    // GIVEN
    var account = Account.openAccountFor("Name", Currency.getInstance("GBP"));
    var addition = oneGPB();

    // WHEN
    var result = account.debit(addition);

    // THEN
    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  public void shouldReturnFailureIfCurrencyDifferentForWithdraw() {
    // GIVEN
    var account = Account.openAccountFor("Name", Currency.getInstance("GBP"));
    var subtract = oneUSD();

    // WHEN
    var result = account.withdraw(subtract);

    // THEN
    assertThat(result.isFailure()).isTrue();
  }

  @Test
  public void shouldSuccessfullyWithdrawMoneyWhenSameValues() {
    // GIVEN
    var account = Account.openAccountFor("Name", Currency.getInstance("GBP"));
    account = account.debit(oneGPB()).successfulValue().orElseThrow();
    var subtract = oneGPB();

    // WHEN
    var result = account.withdraw(subtract);

    // THEN
    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  public void shouldReturnFailureIfYouTryToWithdrawMore() {
    // GIVEN
    var account = Account.openAccountFor("Name", Currency.getInstance("GBP"));
    var subtract = twoGPB();

    // WHEN
    var result = account.withdraw(subtract);

    // THEN
    assertThat(result.isFailure()).isTrue();
  }
}
