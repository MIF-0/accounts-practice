package com.practice.accounts.transfer.domain;

import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.Money;
import com.practice.accounts.shared.TransferId;
import com.practice.accounts.shared.Version;
import java.util.Objects;
import java.util.StringJoiner;

public abstract sealed class Transfer permits InternalTransfer {
  private final TransferId id;
  private final Money money;
  private final Version version;

  protected Transfer(TransferId id, Money money, Version version) {
    this.id = id;
    this.money = money;
    this.version = version;
  }

  public TransferId id() {
    return id;
  }

  public Money money() {
    return money;
  }

  public Version version() {
    return version;
  }

  public abstract boolean belongsTo(AccountId accountId);

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Transfer transfer)) return false;

    return Objects.equals(id, transfer.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Transfer.class.getSimpleName() + "[", "]")
        .add("transferId=" + id)
        .add("money=" + money)
        .add("version=" + version)
        .toString();
  }
}
