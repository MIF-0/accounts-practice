package com.practice.accounts.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

public class FailedTest {

  @Test
  public void shouldReturnTrueForIsFailed() {
    // GIVEN
    Result<Void, String> failure = Failed.failed("test");

    // WHEN THEN
    assertThat(failure.isFailure()).isTrue();
  }

  @Test
  public void shouldNotHaveAdditionalDescription() {
    // GIVEN
    Failed<Void, String> failure = Failed.failed("test");

    // WHEN THEN
    assertThat(failure.additionalDescription()).isEmpty();
  }

  @Test
  public void shouldHaveAdditionalDescription() {
    // GIVEN
    var additional = "Some";
    Failed<Void, String> failure = new Failed<>("test", Optional.of(additional));

    // WHEN THEN
    assertThat(failure.additionalDescription()).isEqualTo(Optional.of(additional));
  }

  @Test
  public void shouldReturnFalseForIsSuccess() {
    // GIVEN
    Result<Void, String> failure = Failed.failed("test");

    // WHEN THEN
    assertThat(failure.isSuccess()).isFalse();
  }

  @Test
  public void shouldReturnProperFailureInfo() {
    // GIVEN
    var failureInfp = "test";
    Result<Void, String> failure = Failed.failed(failureInfp);

    // WHEN THEN
    assertThat(failure.failureInfo()).isEqualTo(Optional.of(failureInfp));
  }

  @Test
  public void shouldReturnEmptySuccessValue() {
    // GIVEN
    Result<Void, String> failure = Failed.failed("test");

    // WHEN THEN
    assertThat(failure.successfulValue()).isEmpty();
  }

  @Test
  public void shouldInvokeFailureMapper() {
    // GIVEN
    Result<Void, String> failure = Failed.failed("test");
    var successMapper =
        new Mapper<Void, Result<String, String>>() {
          @Override
          public Result<String, String> map(Void unused) {
            return new Success<>("");
          }
        };

    var failureMapper =
        new Mapper<String, String>() {
          @Override
          public String map(String s) {
            return s;
          }
        };

    // WHEN
    failure.map(successMapper, failureMapper);

    // THEN
    assertThat(failureMapper.invoked).isTrue();
  }

  @Test
  public void shouldNotInvokeSuccessMapper() {
    // GIVEN
    Result<Void, String> failure = Failed.failed("test");
    var successMapper =
        new Mapper<Void, Result<String, String>>() {
          @Override
          public Result<String, String> map(Void unused) {
            return new Success<>("");
          }
        };

    var failureMapper =
        new Mapper<String, String>() {
          @Override
          public String map(String s) {
            return s;
          }
        };

    // WHEN
    failure.map(successMapper, failureMapper);

    // THEN
    assertThat(successMapper.invoked).isFalse();
  }

  @Test
  public void shouldReturnValueFromFailureMapper() {
    // GIVEN
    Result<Void, String> failure = Failed.failed("test");
    var successMapper =
        new Mapper<Void, Result<String, String>>() {
          @Override
          public Result<String, String> map(Void unused) {
            return new Success<>("");
          }
        };

    var newFailureInfo = "new Failure info";
    var failureMapper =
        new Mapper<String, String>() {
          @Override
          public String map(String s) {
            return newFailureInfo;
          }
        };

    // WHEN
    var result = failure.map(successMapper, failureMapper);

    // THEN
    assertThat(result.isFailure()).isTrue();
    assertThat(result.failureInfo()).isEqualTo(Optional.of(newFailureInfo));
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
