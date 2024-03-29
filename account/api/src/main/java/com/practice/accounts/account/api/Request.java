package com.practice.accounts.account.api;

import com.practice.accounts.shared.RequestId;

public abstract sealed class Request permits AccountCreateRequest, DebitRequest, WithdrawRequest {
  private final RequestId id;

  public Request(RequestId id) {
    this.id = id;
  }

  public RequestId id() {
    return id;
  }
}
