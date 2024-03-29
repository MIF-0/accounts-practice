package com.practice.accounts.account.api;

import com.practice.accounts.account.api.error.AccountBalanceError;
import com.practice.accounts.account.api.error.AccountDuplicate;
import com.practice.accounts.account.api.error.AccountNotFound;
import com.practice.accounts.account.api.error.AccountsError;
import com.practice.accounts.account.api.error.TooManyOperationWithinAccount;
import com.practice.accounts.account.api.event.AccountCreated;
import com.practice.accounts.account.api.event.BalanceUpdated;
import com.practice.accounts.account.domain.Account;
import com.practice.accounts.account.domain.AccountStorage;
import com.practice.accounts.account.domain.error.AccountStorageError;
import com.practice.accounts.account.domain.error.BalanceError;
import com.practice.accounts.shared.Failed;
import com.practice.accounts.shared.Result;
import com.practice.accounts.shared.Success;

public class Accounts {
  private final AccountStorage accountStorage;

  public Accounts(AccountStorage accountStorage) {
    this.accountStorage = accountStorage;
  }

  public Result<AccountCreated, AccountsError> createAccount(AccountCreateRequest request) {
    var account = Account.openAccountFor(request.name(), request.currency());
    return accountStorage
        .insert(account)
        .map(
            success -> new Success<>(new AccountCreated(account.id(), request.id())),
            Accounts::accountsError);
  }

  public Result<BalanceUpdated, AccountsError> withdrawMoneyFor(WithdrawRequest request) {
    var possibleAccount = accountStorage.retrieve(request.accountId());
    if (possibleAccount.isEmpty()) {
      return Failed.failed(new AccountNotFound());
    }
    var account = possibleAccount.get();
    var result = account.withdraw(request.money());
    return processAccountChangeResult(result, request);
  }

  public Result<BalanceUpdated, AccountsError> debitMoneyFor(DebitRequest request) {
    var possibleAccount = accountStorage.retrieve(request.accountId());
    if (possibleAccount.isEmpty()) {
      return Failed.failed(new AccountNotFound());
    }
    var account = possibleAccount.get();
    var result = account.debit(request.money());
    return processAccountChangeResult(result, request);
  }

  private Result<BalanceUpdated, AccountsError> processAccountChangeResult(
      Result<Account, BalanceError> result, Request request) {
    switch (result) {
      case Failed<Account, BalanceError> v -> {
        return new Failed<>(new AccountBalanceError(v.failure()), v.additionalDescription());
      }
      case Success<Account, BalanceError> v -> {
        return persistSuccessfulOperation(v.result(), request);
      }
    }
  }

  private Result<BalanceUpdated, AccountsError> persistSuccessfulOperation(
      Account newAccount, Request request) {
    return accountStorage
        .update(newAccount)
        .map(
            success -> new Success<>(new BalanceUpdated(newAccount.id(), request.id())),
            Accounts::accountsError);
  }

  private static AccountsError accountsError(AccountStorageError accountStorageError) {
    return switch (accountStorageError) {
      case DUPLICATE -> new AccountDuplicate();
      case OPTIMISTIC_LOCKING -> new TooManyOperationWithinAccount();
      case KEY_NOT_FOUND -> new AccountNotFound();
    };
  }
}
