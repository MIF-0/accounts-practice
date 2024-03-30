package com.practice.accounts.transfer.api;

import com.practice.accounts.shared.Failed;
import com.practice.accounts.shared.TransferId;
import com.practice.accounts.transfer.domain.Transfer;
import com.practice.accounts.transfer.domain.TransferStatusError;
import com.practice.accounts.transfer.domain.TransferStorage;
import com.practice.accounts.transfer.domain.WithdrawalService;
import com.practice.accounts.transfer.domain.WithdrawalService.WithdrawalId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PeriodicallyChecker {
  private static final int FETCH_INTERVAL = 1_000;
  private final ScheduledExecutorService executorService;
  private final TransferStorage transferStorage;
  private final WithdrawalService externalService;
  private final ConcurrentMap<WithdrawalId, ScheduledFuture<?>> scheduled;

  public PeriodicallyChecker(TransferStorage transferStorage, WithdrawalService externalService) {
    this.transferStorage = transferStorage;
    this.externalService = externalService;
    this.executorService = new ScheduledThreadPoolExecutor(2);
    this.scheduled = new ConcurrentHashMap<>();
  }

  public void scheduleWithdrawalCheck(WithdrawalId withdrawalId, TransferId transferId) {
    var future =
        executorService.scheduleAtFixedRate(
            () -> {
              var result = externalService.getRequestState(withdrawalId);
              switch (result) {
                case COMPLETED -> {
                  var transferUpdate =
                      transferStorage
                          .retrieve(transferId)
                          .map(Transfer::markAsDone)
                          .orElse(Failed.failed(TransferStatusError.INVALID_STATUS_CHANGE));
                  if (transferUpdate.isSuccess()) {
                    transferStorage.update(transferUpdate.successfulValue().orElseThrow());
                  }
                  scheduled.get(withdrawalId).cancel(false);
                }
                case FAILED -> {
                  var transferUpdate =
                      transferStorage
                          .retrieve(transferId)
                          .map(Transfer::markAsFailed)
                          .orElse(Failed.failed(TransferStatusError.INVALID_STATUS_CHANGE));
                  if (transferUpdate.isSuccess()) {
                    transferStorage.update(transferUpdate.successfulValue().orElseThrow());
                  }
                  scheduled.get(withdrawalId).cancel(false);
                }
              }
            },
            FETCH_INTERVAL,
            FETCH_INTERVAL,
            TimeUnit.MILLISECONDS);
    scheduled.put(withdrawalId, future);
  }
}
