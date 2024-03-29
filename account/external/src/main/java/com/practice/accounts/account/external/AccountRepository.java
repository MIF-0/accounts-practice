package com.practice.accounts.account.external;

import com.practice.accounts.account.domain.Account;
import com.practice.accounts.account.domain.AccountStorage;
import com.practice.accounts.account.domain.error.AccountStorageError;
import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.Failed;
import com.practice.accounts.shared.Result;
import com.practice.accounts.shared.Success;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AccountRepository implements AccountStorage {
  private final Map<AccountId, Account> accounts;

  public AccountRepository() {
    this.accounts = new ConcurrentHashMap<>();
  }

  @Override
  public Result<Void, AccountStorageError> insert(Account account) {
    var result = accounts.putIfAbsent(account.id(), account);
    return result == null ? Success.successVoid() : Failed.failed(AccountStorageError.DUPLICATE);
  }

  @Override
  public Result<Void, AccountStorageError> update(Account account) {
    var result =
        accounts.computeIfPresent(
            account.id(),
            (accountId, oldValue) -> {
              if (account.version().isNextAfter(oldValue.version())) {
                return account;
              }
              return oldValue;
            });

    if (result == null) {
      return Failed.failed(AccountStorageError.KEY_NOT_FOUND);
    }
    // In case of success returned value from map would be same which we try to put,
    // so the reference should be the same
    if (result != account) {
      return Failed.failed(AccountStorageError.OPTIMISTIC_LOCKING);
    }

    return Success.successVoid();
  }

  @Override
  public Optional<Account> retrieve(AccountId accountId) {
    return Optional.ofNullable(accounts.get(accountId));
  }
}
