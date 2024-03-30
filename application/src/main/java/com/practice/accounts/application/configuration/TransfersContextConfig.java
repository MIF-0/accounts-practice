package com.practice.accounts.application.configuration;

import com.practice.accounts.account.api.Accounts;
import com.practice.accounts.transfer.api.Transfers;
import com.practice.accounts.transfer.domain.TransferFactory;
import com.practice.accounts.transfer.domain.TransferStorage;
import com.practice.accounts.transfer.external.WithdrawalServiceStub;
import com.practice.transfers.transfer.external.TransferRepository;

public class TransfersContextConfig {
  private final Transfers transfers;
  private final TransferStorage transferStorage;
  private final TransferFactory transferFactory;

  public TransfersContextConfig(Accounts accounts, Limitation limitation) {
    this.transferStorage = new TransferRepository();
    this.transferFactory = new TransferFactory(limitation.companyAccountId());
    this.transfers =
        new Transfers(transferFactory, transferStorage, accounts, new WithdrawalServiceStub());
  }

  public Transfers transfers() {
    return transfers;
  }

  public TransferStorage transferStorage() {
    return transferStorage;
  }

  public TransferFactory transferFactory() {
    return transferFactory;
  }
}
