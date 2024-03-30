package com.practice.accounts.transfer.domain;

import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.Result;
import com.practice.accounts.shared.TransferId;
import java.util.List;
import java.util.Optional;

public interface TransferStorage {

  Result<Void, TransferStorageError> insert(Transfer transfer);

  Result<Void, TransferStorageError> update(Transfer transfer);

  Optional<Transfer> retrieve(TransferId transferId);

  List<Transfer> retrieveFor(AccountId accountId);
}
