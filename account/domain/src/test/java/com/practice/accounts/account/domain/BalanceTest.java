package com.practice.accounts.account.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.practice.accounts.shared.MoneyFactory;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class BalanceTest implements MoneyFactory {

  @Test
  public void shouldReturnZeroForEmptyBalance() {
    // GIVEN
    var balance = Balance.emptyBalance(Currency.getInstance("GBP"));

    // WHEN
    var result = balance.value();

    // THEN
    assertThat(result).isEqualTo(BigDecimal.ZERO);
  }

  @Test
  public void shouldReturnFailureIfCurrencyDifferentForDebit() {
    // GIVEN
    var balance = Balance.emptyBalance(Currency.getInstance("GBP"));
    var addition = oneUSD();
    // WHEN
    var result = balance.debit(addition);

    // THEN
    assertThat(result.isFailure()).isTrue();
  }

  @Test
  public void shouldSuccessfullyDebitMoney() {
    // GIVEN
    var balance = Balance.emptyBalance(Currency.getInstance("GBP"));
    var addition = oneGPB();
    // WHEN
    var result = balance.debit(addition);

    // THEN
    var expectedBalance = new Balance(oneGPB());
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.successfulValue()).isEqualTo(Optional.of(expectedBalance));
  }

  @Test
  public void shouldReturnFailureIfCurrencyDifferentForWithdraw() {
    // GIVEN
    var balance = Balance.emptyBalance(Currency.getInstance("GBP"));
    var subtract = oneUSD();
    // WHEN
    var result = balance.withdraw(subtract);

    // THEN
    assertThat(result.isFailure()).isTrue();
  }

  @Test
  public void shouldSuccessfullyWithdrawMoneyWhenSameValues() {
    // GIVEN
    var balance = new Balance(oneGPB());
    var subtract = oneGPB();
    // WHEN
    var result = balance.withdraw(subtract);

    // THEN
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.successfulValue())
        .isEqualTo(Optional.of(Balance.emptyBalance(Currency.getInstance("GBP"))));
  }

  @Test
  public void shouldSuccessfullyWithdrawMoneyWhenYouHaveMore() {
    // GIVEN
    var balance = new Balance(twoGPB());
    var subtract = oneGPB();
    // WHEN
    var result = balance.withdraw(subtract);

    // THEN
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.successfulValue()).isEqualTo(Optional.of(new Balance(oneGPB())));
  }

  @Test
  public void shouldReturnFailureIfYouTryToWithdrawMore() {
    // GIVEN
    var balance = Balance.emptyBalance(Currency.getInstance("GBP"));
    var subtract = twoGPB();
    // WHEN
    var result = balance.withdraw(subtract);

    // THEN
    assertThat(result.isFailure()).isTrue();
  }
}
