package com.practice.accounts.shared;

import java.util.UUID;

public record TransferId(UUID value) {
  public static TransferId generate() {
    return new TransferId(UUID.randomUUID());
  }
}
