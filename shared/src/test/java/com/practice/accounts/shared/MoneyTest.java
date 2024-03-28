package com.practice.accounts.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;

public class MoneyTest implements MoneyFactory {

  @Test
  public void shouldReturnFailureIfCurrencyDifferentForAdd() {
    // GIVEN
    var money = oneGPB();
    var addition = oneUSD();

    // WHEN
    var result = money.add(addition);

    // THEN
    assertThat(result.isFailure()).isTrue();
  }

  @Test
  public void shouldSuccessfullyAddMoney() {
    // GIVEN
    var money = oneGPB();
    var addition = oneGPB();

    // WHEN
    var result = money.add(addition);

    // THEN
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.successfulValue()).isEqualTo(Optional.of(twoGPB()));
  }

  @Test
  public void shouldReturnFailureIfCurrencyDifferentForSubtraction() {
    // GIVEN
    var money = oneGPB();
    var subtract = oneUSD();
    // WHEN
    var result = money.subtract(subtract);

    // THEN
    assertThat(result.isFailure()).isTrue();
  }

  @Test
  public void shouldSuccessfullySubtractMoneyWhenSameValues() {
    // GIVEN
    var money = oneGPB();
    var subtract = oneGPB();

    // WHEN
    var result = money.subtract(subtract);

    // THEN
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.successfulValue()).isEqualTo(Optional.of(zeroGPB()));
  }

  @Test
  public void shouldSuccessfullySubtractMoneyWhenYouHaveMore() {
    // GIVEN
    var money = twoGPB();
    var subtract = oneGPB();
    // WHEN
    var result = money.subtract(subtract);

    // THEN
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.successfulValue()).isEqualTo(Optional.of(oneGPB()));
  }

  @Test
  public void shouldReturnFailureIfYouTryToSubtractMore() {
    // GIVEN
    var money = oneGPB();
    var subtract = twoGPB();
    // WHEN
    var result = money.subtract(subtract);

    // THEN
    assertThat(result.isFailure()).isTrue();
  }
}
