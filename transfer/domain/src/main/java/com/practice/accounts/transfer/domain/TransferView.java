package com.practice.accounts.transfer.domain;

import java.util.StringJoiner;

public class TransferView {
  private final Transfer transfer;

  public TransferView(Transfer transfer) {
    this.transfer = transfer;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "Transfer [", "]")
        .add("id=" + transfer.id().value().toString())
        .add("status=" + transfer.status().toString())
        .toString();
  }
}
