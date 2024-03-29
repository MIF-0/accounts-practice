package com.practice.accounts.shared;

import java.util.UUID;

public record RequestId(String value) {
  public static RequestId generate() {
    return new RequestId(UUID.randomUUID().toString());
  }
}
