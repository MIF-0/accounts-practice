package com.practice.accounts.transfer.api;

import com.practice.accounts.account.api.Accounts;
import com.practice.accounts.account.api.DebitRequest;
import com.practice.accounts.account.api.WithdrawRequest;
import com.practice.accounts.account.api.error.AccountBalanceError;
import com.practice.accounts.account.api.error.AccountDuplicate;
import com.practice.accounts.account.api.error.AccountNotFound;
import com.practice.accounts.account.api.error.AccountsError;
import com.practice.accounts.account.api.error.TooManyOperationWithinAccount;
import com.practice.accounts.account.api.event.BalanceUpdated;
import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.Failed;
import com.practice.accounts.shared.RequestId;
import com.practice.accounts.shared.Result;
import com.practice.accounts.shared.Success;
import com.practice.accounts.transfer.domain.ExternalTransfer;
import com.practice.accounts.transfer.domain.InternalTransfer;
import com.practice.accounts.transfer.domain.Transfer;
import com.practice.accounts.transfer.domain.TransferFactory;
import com.practice.accounts.transfer.domain.TransferStorage;
import com.practice.accounts.transfer.domain.TransferView;
import com.practice.accounts.transfer.domain.WithdrawalService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Transfers {
  private static final Logger LOGGER = LoggerFactory.getLogger(Transfers.class);

  private final TransferFactory transferFactory;
  private final TransferStorage transferStorage;
  private final Accounts accounts;
  private final WithdrawalService externalService;
  private final PeriodicallyChecker periodicallyChecker;

  public Transfers(
      TransferFactory transferFactory,
      TransferStorage transferStorage,
      Accounts accounts,
      WithdrawalService externalService) {
    this.transferFactory = transferFactory;
    this.transferStorage = transferStorage;
    this.accounts = accounts;
    this.externalService = externalService;
    this.periodicallyChecker = new PeriodicallyChecker(transferStorage, externalService);
  }

  public Result<Void, TransferError> topUp(TopUpRequest topUpRequest) {
    LOGGER.info("Top up request by " + topUpRequest.receiver().accountId());
    var transfer = transferFactory.newTopUp(topUpRequest.money(), topUpRequest.receiver());
    var result = processInternalTransfer(transfer);
    LOGGER.info(
        "Top up finished for "
            + topUpRequest.receiver().accountId()
            + (result.isSuccess() ? " with success." : "and it failed"));
    return result;
  }

  public Result<Void, TransferError> internalTransfer(InternalTransferRequest transferRequest) {
    LOGGER.info("Top up request by " + transferRequest.sender().accountId());
    var transfer =
        transferFactory.newInternalTransfer(
            transferRequest.money(), transferRequest.receiver(), transferRequest.sender());
    var result = processInternalTransfer(transfer);
    LOGGER.info(
        "Top up finished for "
            + transferRequest.sender().accountId()
            + (result.isSuccess() ? " with success." : "and it failed"));
    return result;
  }

  public Result<Void, TransferError> externalTransfer(ExternalTransferRequest transferRequest) {
    LOGGER.info("Top up request by " + transferRequest.sender().accountId());
    var transfer =
        transferFactory.newExternalTransfer(
            transferRequest.money(), transferRequest.receiver(), transferRequest.sender());
    var result = processExternalTransfer(transfer);
    LOGGER.info(
        "Top up finished for "
            + transferRequest.sender().accountId()
            + (result.isSuccess() ? " with success." : "and it failed"));
    return result;
  }

  public List<TransferView> transfersFor(AccountId accountId) {
    return transferStorage.retrieveFor(accountId).stream().map(TransferView::new).toList();
  }

  // Ideally it should be done via events and separate consumers not to mix everything here
  private Result<Void, TransferError> processInternalTransfer(InternalTransfer transfer) {
    var insertResult = transferStorage.insert(transfer);
    if (insertResult.isFailure()) {
      return Failed.failed(TransferError.UNABLE_TO_STORE_TRANSFER);
    }

    var withdrawRequest =
        new WithdrawRequest(
            new RequestId(transfer.id().value().toString()),
            transfer.sender().accountId(),
            transfer.money());
    var withdrawalResult = processWithdrawal(transfer, withdrawRequest);
    return switch (withdrawalResult) {
      case Failed<Transfer, TransferError> value -> Failed.failed(value.failure());
      case Success<Transfer, TransferError> value -> {
        var debitRequest =
            new DebitRequest(
                new RequestId(transfer.id().value().toString()),
                transfer.receiver().accountId(),
                transfer.money());
        yield processDebit(value.result(), debitRequest, withdrawRequest);
      }
    };
  }

  private Result<Void, TransferError> processExternalTransfer(ExternalTransfer transfer) {
    var insertResult = transferStorage.insert(transfer);
    if (insertResult.isFailure()) {
      return Failed.failed(TransferError.UNABLE_TO_STORE_TRANSFER);
    }

    var withdrawRequest =
        new WithdrawRequest(
            new RequestId(transfer.id().value().toString()),
            transfer.sender().accountId(),
            transfer.money());
    var withdrawalResult = processWithdrawal(transfer, withdrawRequest);
    return switch (withdrawalResult) {
      case Failed<Transfer, TransferError> value -> Failed.failed(value.failure());
      case Success<Transfer, TransferError> value ->
          sendToExternal(value.result(), transfer.receiver());
    };
  }

  private Result<Void, TransferError> sendToExternal(
      Transfer transfer, WithdrawalService.Address receiver) {
    WithdrawalService.WithdrawalId withdrawalId =
        new WithdrawalService.WithdrawalId(transfer.id().value());
    externalService.requestWithdrawal(withdrawalId, receiver, transfer.money());
    periodicallyChecker.scheduleWithdrawalCheck(withdrawalId, transfer.id());
    return Success.successVoid();
  }

  private Result<Transfer, TransferError> processWithdrawal(
      Transfer transfer, WithdrawRequest withdrawRequest) {
    var result = accounts.withdrawMoneyFor(withdrawRequest);
    return switch (result) {
      case Failed<BalanceUpdated, AccountsError> v -> {
        var failedTransfer = transfer.markAsFailed();
        if (failedTransfer.isSuccess()) {
          transferStorage.update(failedTransfer.successfulValue().orElseThrow());
        }
        yield Failed.failed(mapAccountToTransferError(v.failure(), TransferError.WITHDRAW_FAILED));
      }
      case Success<BalanceUpdated, AccountsError> v -> {
        var updatedTransfer = transfer.withdrawFinished();
        if (updatedTransfer.isFailure()) {
          rollbackWithdraw(withdrawRequest);
          yield Failed.failed(TransferError.UNABLE_TO_STORE_TRANSFER);
        }
        var partiallyCompletedTransfer = updatedTransfer.successfulValue().orElseThrow();
        var updateResult = transferStorage.update(partiallyCompletedTransfer);
        if (updateResult.isFailure()) {
          rollbackWithdraw(withdrawRequest);
          yield Failed.failed(TransferError.UNABLE_TO_STORE_TRANSFER);
        }
        yield new Success<>(partiallyCompletedTransfer);
      }
    };
  }

  private Result<Void, TransferError> processDebit(
      Transfer transfer, DebitRequest debitRequest, WithdrawRequest withdrawRequest) {
    var debitResult = accounts.debitMoneyFor(debitRequest);
    return switch (debitResult) {
      case Failed<BalanceUpdated, AccountsError> v -> {
        var failedTransfer = transfer.markAsFailed();
        if (failedTransfer.isSuccess()) {
          transferStorage.update(failedTransfer.successfulValue().orElseThrow());
        }
        rollbackWithdraw(withdrawRequest);
        yield Failed.failed(mapAccountToTransferError(v.failure(), TransferError.DEBIT_FAILED));
      }
      case Success<BalanceUpdated, AccountsError> v -> {
        var completedTransfer = transfer.markAsDone();
        if (completedTransfer.isFailure()) {
          rollbackDebit(debitRequest);
          rollbackWithdraw(withdrawRequest);
          yield Failed.failed(TransferError.UNABLE_TO_STORE_TRANSFER);
        }
        var updateFullResult =
            transferStorage.update(completedTransfer.successfulValue().orElseThrow());
        if (updateFullResult.isFailure()) {
          rollbackDebit(debitRequest);
          rollbackWithdraw(withdrawRequest);
          yield Failed.failed(TransferError.UNABLE_TO_STORE_TRANSFER);
        }
        yield Success.successVoid();
      }
    };
  }

  private void rollbackWithdraw(WithdrawRequest withdrawRequest) {
    var debitRequest =
        new DebitRequest(
            withdrawRequest.id(), withdrawRequest.accountId(), withdrawRequest.money());
    while (accounts.debitMoneyFor(debitRequest).isFailure())
      ;
  }

  private void rollbackDebit(DebitRequest debitRequest) {
    var withdrawRequest =
        new WithdrawRequest(debitRequest.id(), debitRequest.accountId(), debitRequest.money());
    while (accounts.withdrawMoneyFor(withdrawRequest).isFailure())
      ;
  }

  private TransferError mapAccountToTransferError(
      AccountsError failure, TransferError defaultError) {
    return switch (failure) {
      case AccountBalanceError unused -> defaultError;
      case AccountDuplicate unused -> defaultError;
      case AccountNotFound unused -> defaultError;
      case TooManyOperationWithinAccount unused -> TransferError.TOO_MANY_OPERATION;
    };
  }
}
