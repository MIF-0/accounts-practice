package com.practice.accounts.account.domain;

import com.practice.accounts.account.domain.error.AccountStorageError;
import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.Result;
import java.util.Optional;

public interface AccountStorage {
  Result<Void, AccountStorageError> insert(Account account);

  Result<Void, AccountStorageError> update(Account account);

  Optional<Account> retrieve(AccountId accountId);
}
