package com.practice.accounts.shared;

import java.util.Optional;
import java.util.function.Function;

public record Success<SUCCESS, FAILURE>(SUCCESS result) implements Result<SUCCESS, FAILURE> {
  @Override
  public boolean isSuccess() {
    return true;
  }

  @Override
  public <NEW_SUCCESS, NEW_FAILURE> Result<NEW_SUCCESS, NEW_FAILURE> map(
      Function<SUCCESS, Result<NEW_SUCCESS, NEW_FAILURE>> successMapper,
      Function<FAILURE, NEW_FAILURE> failureMapper) {
    return successMapper.apply(result);
  }

  @Override
  public Optional<SUCCESS> successfulValue() {
    return Optional.of(result);
  }
}
