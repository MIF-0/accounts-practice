package com.practice.transfers.transfer.external;

import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.Failed;
import com.practice.accounts.shared.Result;
import com.practice.accounts.shared.Success;
import com.practice.accounts.shared.TransferId;
import com.practice.accounts.transfer.domain.Transfer;
import com.practice.accounts.transfer.domain.TransferStorage;
import com.practice.accounts.transfer.domain.TransferStorageError;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TransferRepository implements TransferStorage {
  private final Map<TransferId, Transfer> transfers;

  public TransferRepository() {
    this.transfers = new ConcurrentHashMap<>();
  }

  @Override
  public Result<Void, TransferStorageError> insert(Transfer transfer) {
    var result = transfers.putIfAbsent(transfer.id(), transfer);
    return result == null ? Success.successVoid() : Failed.failed(TransferStorageError.DUPLICATE);
  }

  @Override
  public Result<Void, TransferStorageError> update(Transfer transfer) {
    var result =
        transfers.computeIfPresent(
            transfer.id(),
            (transferId, oldValue) -> {
              if (transfer.version().isNextAfter(oldValue.version())) {
                return transfer;
              }
              return oldValue;
            });

    if (result == null) {
      return Failed.failed(TransferStorageError.KEY_NOT_FOUND);
    }
    // In case of success returned value from map would be same which we try to put,
    // so the reference should be the same
    if (result != transfer) {
      return Failed.failed(TransferStorageError.OPTIMISTIC_LOCKING);
    }

    return Success.successVoid();
  }

  @Override
  public Optional<Transfer> retrieve(TransferId transferId) {
    return Optional.ofNullable(transfers.get(transferId));
  }

  @Override
  public List<Transfer> retrieveFor(AccountId accountId) {
    return transfers.values().stream().filter(transfer -> transfer.belongsTo(accountId)).toList();
  }
}
