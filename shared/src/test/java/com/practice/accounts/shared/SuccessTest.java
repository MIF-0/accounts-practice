package com.practice.accounts.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

public class SuccessTest {

  @Test
  public void shouldReturnTrueForIsSuccess() {
    // GIVEN
    Result<String, Void> success = new Success<>("test");

    // WHEN THEN
    assertThat(success.isSuccess()).isTrue();
  }

  @Test
  public void shouldReturnFalseForIsFailure() {
    // GIVEN
    Result<String, Void> success = new Success<>("test");

    // WHEN THEN
    assertThat(success.isFailure()).isFalse();
  }

  @Test
  public void shouldReturnProperSuccessValue() {
    // GIVEN
    var successResult = "test";
    Result<String, Void> success = new Success<>(successResult);

    // WHEN THEN
    assertThat(success.successfulValue()).isEqualTo(Optional.of(successResult));
  }

  @Test
  public void shouldReturnEmptyFailureInfo() {
    // GIVEN
    Result<String, Void> failure = new Success<>("test");

    // WHEN THEN
    assertThat(failure.failureInfo()).isEmpty();
  }

  @Test
  public void shouldInvokeSuccessMapper() {
    // GIVEN
    Result<String, Void> success = new Success<>("test");
    var successMapper =
        new FailedTest.Mapper<String, Result<String, String>>() {
          @Override
          public Result<String, String> map(String value) {
            return new Success<>(value);
          }
        };

    var failureMapper =
        new Mapper<Void, String>() {
          @Override
          public String map(Void s) {
            return "failure";
          }
        };

    // WHEN
    success.map(successMapper, failureMapper);

    // THEN
    assertThat(successMapper.invoked).isTrue();
  }

  @Test
  public void shouldNotInvokeFailureMapper() {
    // GIVEN
    Result<String, Void> success = new Success<>("test");
    var successMapper =
        new FailedTest.Mapper<String, Result<String, String>>() {
          @Override
          public Result<String, String> map(String value) {
            return new Success<>(value);
          }
        };

    var failureMapper =
        new Mapper<Void, String>() {
          @Override
          public String map(Void s) {
            return "failure";
          }
        };

    // WHEN
    success.map(successMapper, failureMapper);

    // THEN
    assertThat(failureMapper.invoked).isFalse();
  }

  @Test
  public void shouldReturnValueFromSuccessMapperMapper() {
    // GIVEN
    Result<String, Void> success = new Success<>("test");
    var newSuccessResult = "new result";

    var successMapper =
        new FailedTest.Mapper<String, Result<String, String>>() {
          @Override
          public Result<String, String> map(String unused) {
            return new Success<>(newSuccessResult);
          }
        };

    var failureMapper =
        new Mapper<Void, String>() {
          @Override
          public String map(Void s) {
            return "failure";
          }
        };

    // WHEN
    var result = success.map(successMapper, failureMapper);

    // THEN
    assertThat(successMapper.invoked).isTrue();
    assertThat(result.successfulValue()).isEqualTo(Optional.of(newSuccessResult));
  }

  abstract static class Mapper<T, V> implements Function<T, V> {
    boolean invoked;

    public Mapper() {
      this.invoked = false;
    }

    @Override
    public V apply(T t) {
      this.invoked = true;
      return map(t);
    }

    public abstract V map(T t);
  }
}
