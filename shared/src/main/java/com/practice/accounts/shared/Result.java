package com.practice.accounts.shared;

import java.util.Optional;
import java.util.function.Function;

// Don't get me wrong. I decided to experiment with this as I don't like exception programming
public sealed interface Result<SUCCESS, FAILURE> permits Failed, Success {

  <NEW_SUCCESS, NEW_FAILURE> Result<NEW_SUCCESS, NEW_FAILURE> map(
      Function<SUCCESS, Result<NEW_SUCCESS, NEW_FAILURE>> successMapper,
      Function<FAILURE, NEW_FAILURE> failureMapper);

  default Optional<SUCCESS> successfulValue() {
    return Optional.empty();
  }

  default Optional<FAILURE> failureInfo() {
    return Optional.empty();
  }

  default boolean isSuccess() {
    return false;
  }

  default boolean isFailure() {
    return false;
  }
}
