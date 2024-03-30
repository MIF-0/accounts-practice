package com.practice.accounts.transfer.domain;

import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.Failed;
import com.practice.accounts.shared.Money;
import com.practice.accounts.shared.Result;
import com.practice.accounts.shared.Success;
import com.practice.accounts.shared.TransferId;
import com.practice.accounts.shared.Version;
import com.practice.accounts.transfer.domain.WithdrawalService.Address;

public final class ExternalTransfer extends Transfer {
  private final Status status;
  private final Sender sender;
  private final Address receiver;

  ExternalTransfer(Money money, Sender sender, Address receiver) {
    super(TransferId.generate(), money, Version.createFirstVersion());
    this.status = Status.NEW;
    this.sender = sender;
    this.receiver = receiver;
  }

  private ExternalTransfer(ExternalTransfer previous, Status newStatus) {
    super(previous.id(), previous.money(), previous.version().next());
    this.status = newStatus;
    this.sender = previous.sender;
    this.receiver = previous.receiver;
  }

  @Override
  public Result<Transfer, TransferStatusError> withdrawFinished() {
    if (this.status.equals(Status.NEW)) {
      return new Success<>(new ExternalTransfer(this, Status.WITHDRAW_DONE));
    } else {
      return Failed.failed(TransferStatusError.INVALID_STATUS_CHANGE);
    }
  }

  @Override
  public Result<Transfer, TransferStatusError> markAsDone() {
    if (this.status.equals(Status.WITHDRAW_DONE)) {
      return new Success<>(new ExternalTransfer(this, Status.FULLY_DONE));
    } else {
      return Failed.failed(TransferStatusError.INVALID_STATUS_CHANGE);
    }
  }

  @Override
  public Result<Transfer, TransferStatusError> markAsFailed() {
    if (this.status.equals(Status.FULLY_DONE)) {
      return Failed.failed(TransferStatusError.INVALID_STATUS_CHANGE);
    } else {
      return new Success<>(new ExternalTransfer(this, Status.FULLY_DONE));
    }
  }

  Status status() {
    return status;
  }

  public Sender sender() {
    return sender;
  }

  public Address receiver() {
    return receiver;
  }

  @Override
  public boolean belongsTo(AccountId accountId) {
    return sender.accountId().equals(accountId);
  }

  enum Status {
    NEW,
    WITHDRAW_DONE,
    FULLY_DONE,
    FAILED,
  }
}
