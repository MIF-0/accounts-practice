package com.practice.accounts.shared;

import java.util.UUID;

public record AccountId(UUID value) {
  public static AccountId generate() {
    return new AccountId(UUID.randomUUID());
  }

  public static AccountId from(String uuid) {
    return new AccountId(UUID.fromString(uuid));
  }
}
