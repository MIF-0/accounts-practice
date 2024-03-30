package com.practice.accounts.transfer.domain;

import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.Money;

public class TransferFactory {
  private final AccountId companyId;

  public TransferFactory(AccountId companyId) {
    this.companyId = companyId;
  }

  public InternalTransfer newTopUp(Money money, Receiver receiver) {
    return new InternalTransfer(money, new Sender(companyId), receiver);
  }
}
