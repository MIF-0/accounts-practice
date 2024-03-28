package com.practice.accounts.shared;

import java.util.Optional;
import java.util.function.Function;

public record Failed<SUCCESS, FAILURE>(FAILURE failure, Optional<String> additionalDescription)
    implements Result<SUCCESS, FAILURE> {

  public static <SUCCESS, FAILURE> Failed<SUCCESS, FAILURE> failed(FAILURE failure) {
    return new Failed<>(failure, Optional.empty());
  }

  @Override
  public boolean isFailure() {
    return true;
  }

  @Override
  public <NEW_SUCCESS, NEW_FAILURE> Result<NEW_SUCCESS, NEW_FAILURE> map(
      Function<SUCCESS, Result<NEW_SUCCESS, NEW_FAILURE>> successMapper,
      Function<FAILURE, NEW_FAILURE> failureMapper) {
    return new Failed<>(failureMapper.apply(failure), additionalDescription);
  }

  @Override
  public Optional<FAILURE> failureInfo() {
    return Optional.of(failure);
  }
}
