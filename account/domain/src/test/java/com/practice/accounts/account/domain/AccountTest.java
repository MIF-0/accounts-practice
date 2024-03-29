package com.practice.accounts.account.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.practice.accounts.shared.MoneyFactory;
import org.junit.jupiter.api.Test;

public class AccountTest implements MoneyFactory, AccountFactory {

  @Test
  public void shouldReturnFailureIfCurrencyDifferentForDebit() {
    // GIVEN
    var account = newGBPAccount();
    var addition = oneUSD();
    // WHEN
    var result = account.debit(addition);

    // THEN
    assertThat(result.isFailure()).isTrue();
  }

  @Test
  public void shouldSuccessfullyDebitMoney() {
    // GIVEN
    var account = newGBPAccount();
    var addition = oneGPB();

    // WHEN
    var result = account.debit(addition);

    // THEN
    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  public void shouldReturnFailureIfCurrencyDifferentForWithdraw() {
    // GIVEN
    var account = newGBPAccount();
    var subtract = oneUSD();

    // WHEN
    var result = account.withdraw(subtract);

    // THEN
    assertThat(result.isFailure()).isTrue();
  }

  @Test
  public void shouldSuccessfullyWithdrawMoneyWhenSameValues() {
    // GIVEN
    var account = newGBPAccount();
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
    var account = newGBPAccount();
    var subtract = twoGPB();

    // WHEN
    var result = account.withdraw(subtract);

    // THEN
    assertThat(result.isFailure()).isTrue();
  }
}
