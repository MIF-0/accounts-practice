package com.practice.accounts.transfer.domain;

import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.Failed;
import com.practice.accounts.shared.Money;
import com.practice.accounts.shared.Result;
import com.practice.accounts.shared.Success;
import com.practice.accounts.shared.TransferId;
import com.practice.accounts.shared.Version;

public final class InternalTransfer extends Transfer {
  private final Status status;
  private final Sender sender;
  private final Receiver receiver;

  InternalTransfer(Money money, Sender sender, Receiver receiver) {
    super(TransferId.generate(), money, Version.createFirstVersion());
    this.status = Status.NEW;
    this.sender = sender;
    this.receiver = receiver;
  }

  private InternalTransfer(InternalTransfer previous, Status newStatus) {
    super(previous.id(), previous.money(), previous.version().next());
    this.status = newStatus;
    this.sender = previous.sender;
    this.receiver = previous.receiver;
  }

  public Result<InternalTransfer, TransferStatusError> withdrawFinished() {
    if (this.status.equals(Status.NEW)) {
      return new Success<>(new InternalTransfer(this, Status.WITHDRAW_DONE));
    } else {
      return Failed.failed(TransferStatusError.INVALID_STATUS_CHANGE);
    }
  }

  public Result<InternalTransfer, TransferStatusError> done() {
    if (this.status.equals(Status.WITHDRAW_DONE)) {
      return new Success<>(new InternalTransfer(this, Status.FULLY_DONE));
    } else {
      return Failed.failed(TransferStatusError.INVALID_STATUS_CHANGE);
    }
  }

  public Result<InternalTransfer, TransferStatusError> failed() {
    if (this.status.equals(Status.FULLY_DONE)) {
      return Failed.failed(TransferStatusError.INVALID_STATUS_CHANGE);
    } else {
      return new Success<>(new InternalTransfer(this, Status.FULLY_DONE));
    }
  }

  Status status() {
    return status;
  }

  public Sender sender() {
    return sender;
  }

  public Receiver receiver() {
    return receiver;
  }

  @Override
  public boolean belongsTo(AccountId accountId) {
    return sender.accountId().equals(accountId) || receiver.accountId().equals(accountId);
  }

  enum Status {
    NEW,
    WITHDRAW_DONE,
    FULLY_DONE,
    FAILED,
  }
}
