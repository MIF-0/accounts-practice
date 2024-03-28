package com.practice.accounts.account.api;

import com.practice.accounts.account.api.error.AccountBalanceError;
import com.practice.accounts.account.api.error.AccountDuplicate;
import com.practice.accounts.account.api.error.AccountNotFound;
import com.practice.accounts.account.api.error.AccountsError;
import com.practice.accounts.account.api.error.TooManyOperationWithinAccount;
import com.practice.accounts.account.domain.Account;
import com.practice.accounts.account.domain.AccountStorage;
import com.practice.accounts.account.domain.error.AccountStorageError;
import com.practice.accounts.account.domain.error.BalanceError;
import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.Failed;
import com.practice.accounts.shared.Money;
import com.practice.accounts.shared.Result;
import com.practice.accounts.shared.Success;
import java.util.Currency;

public class Accounts {
  private final AccountStorage accountStorage;

  public Accounts(AccountStorage accountStorage) {
    this.accountStorage = accountStorage;
  }

  public Result<AccountId, AccountsError> createAccount(String name, Currency currency) {
    var account = Account.openAccountFor(name, currency);
    return accountStorage
        .insert(account)
        .map(success -> new Success<>(account.accountId()), Accounts::accountsError);
  }

  public Result<Void, AccountsError> withdrawMoneyFor(AccountId accountId, Money money) {
    var possibleAccount = accountStorage.retrieve(accountId);
    if (possibleAccount.isEmpty()) {
      return Failed.failed(new AccountNotFound());
    }
    var account = possibleAccount.get();
    var result = account.withdraw(money);
    return processAccountChangeResult(result);
  }

  public Result<Void, AccountsError> debitMoneyFor(AccountId accountId, Money money) {
    var possibleAccount = accountStorage.retrieve(accountId);
    if (possibleAccount.isEmpty()) {
      return Failed.failed(new AccountNotFound());
    }
    var account = possibleAccount.get();
    var result = account.debit(money);
    return processAccountChangeResult(result);
  }

  private Result<Void, AccountsError> processAccountChangeResult(
      Result<Account, BalanceError> result) {
    switch (result) {
      case Failed<Account, BalanceError> v -> {
        return new Failed<>(new AccountBalanceError(v.failure()), v.additionalDescription());
      }
      case Success<Account, BalanceError> v -> {
        return persistSuccessfulOperation(v.result());
      }
    }
  }

  private Result<Void, AccountsError> persistSuccessfulOperation(Account newAccount) {
    return accountStorage
        .update(newAccount)
        .map(success -> Success.successVoid(), Accounts::accountsError);
  }

  private static AccountsError accountsError(AccountStorageError accountStorageError) {
    return switch (accountStorageError) {
      case DUPLICATE -> new AccountDuplicate();
      case OPTIMISTIC_LOCKING -> new TooManyOperationWithinAccount();
    };
  }
}
