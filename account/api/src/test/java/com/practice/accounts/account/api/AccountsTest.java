package com.practice.accounts.account.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.practice.accounts.account.api.error.AccountBalanceError;
import com.practice.accounts.account.api.error.AccountDuplicate;
import com.practice.accounts.account.api.error.AccountNotFound;
import com.practice.accounts.account.api.error.TooManyOperationWithinAccount;
import com.practice.accounts.account.domain.Account;
import com.practice.accounts.account.domain.AccountStorage;
import com.practice.accounts.account.domain.error.AccountStorageError;
import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.Failed;
import com.practice.accounts.shared.MoneyFactory;
import com.practice.accounts.shared.Success;
import java.util.Currency;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class AccountsTest implements MoneyFactory {

  @Test
  public void shouldSuccessfullyCreateAccount() {
    // GIVEN
    var accountStorage = mock(AccountStorage.class);
    given(accountStorage.insert(any())).willReturn(Success.successVoid());
    var accounts = new Accounts(accountStorage);

    // WHEN
    var result = accounts.createAccount("Name", Currency.getInstance("GBP"));

    // THEN
    verify(accountStorage).insert(any());
    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  public void shouldReturnFailedIfDuplicate() {
    // GIVEN
    var accountStorage = mock(AccountStorage.class);
    given(accountStorage.insert(any())).willReturn(Failed.failed(AccountStorageError.DUPLICATE));
    var accounts = new Accounts(accountStorage);

    // WHEN
    var result = accounts.createAccount("Name", Currency.getInstance("GBP"));

    // THEN
    verify(accountStorage).insert(any());
    assertThat(result.isFailure()).isTrue();
    assertThat(result.failureInfo().get()).isInstanceOf(AccountDuplicate.class);
  }

  @Test
  public void shouldSuccessfullyDebitAccount() {
    // GIVEN
    var account = Account.openAccountFor("Name", Currency.getInstance("GBP"));
    var accountStorage = mock(AccountStorage.class);
    given(accountStorage.update(any())).willReturn(Success.successVoid());
    given(accountStorage.retrieve(account.accountId())).willReturn(Optional.of(account));
    var accounts = new Accounts(accountStorage);

    // WHEN
    var result = accounts.debitMoneyFor(account.accountId(), oneGPB());

    // THEN
    verify(accountStorage).retrieve(account.accountId());
    verify(accountStorage).update(any());
    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  public void shouldReturnFailureIfNoAccountDuringDebit() {
    // GIVEN
    var accountId = AccountId.generate();
    var accountStorage = mock(AccountStorage.class);
    given(accountStorage.retrieve(accountId)).willReturn(Optional.empty());
    var accounts = new Accounts(accountStorage);

    // WHEN
    var result = accounts.debitMoneyFor(accountId, oneGPB());

    // THEN
    verify(accountStorage).retrieve(accountId);
    verify(accountStorage, never()).update(any());
    assertThat(result.isFailure()).isTrue();
    assertThat(result.failureInfo().get()).isInstanceOf(AccountNotFound.class);
  }

  @Test
  public void shouldReturnFailureIfBalanceIssueDuringDebit() {
    // GIVEN
    var account = Account.openAccountFor("Name", Currency.getInstance("USD"));
    var accountStorage = mock(AccountStorage.class);
    given(accountStorage.retrieve(account.accountId())).willReturn(Optional.of(account));
    var accounts = new Accounts(accountStorage);

    // WHEN
    var result = accounts.debitMoneyFor(account.accountId(), oneGPB());

    // THEN
    verify(accountStorage).retrieve(account.accountId());
    verify(accountStorage, never()).update(any());
    assertThat(result.isFailure()).isTrue();
    assertThat(result.failureInfo().get()).isInstanceOf(AccountBalanceError.class);
  }

  @Test
  public void shouldReturnFailureIfStorageIssuesDuringDebitAccount() {
    // GIVEN
    var account = Account.openAccountFor("Name", Currency.getInstance("GBP"));
    var accountStorage = mock(AccountStorage.class);
    given(accountStorage.update(any()))
        .willReturn(Failed.failed(AccountStorageError.OPTIMISTIC_LOCKING));
    given(accountStorage.retrieve(account.accountId())).willReturn(Optional.of(account));
    var accounts = new Accounts(accountStorage);

    // WHEN
    var result = accounts.debitMoneyFor(account.accountId(), oneGPB());

    // THEN
    verify(accountStorage).retrieve(account.accountId());
    verify(accountStorage).update(any());
    assertThat(result.isFailure()).isTrue();
    assertThat(result.failureInfo().get()).isInstanceOf(TooManyOperationWithinAccount.class);
  }

  @Test
  public void shouldSuccessfullyWithdrawFromAccount() {
    // GIVEN
    var account = Account.openAccountFor("Name", Currency.getInstance("GBP"));
    account = account.debit(twoGPB()).successfulValue().orElseThrow();
    var accountStorage = mock(AccountStorage.class);
    given(accountStorage.update(any())).willReturn(Success.successVoid());
    given(accountStorage.retrieve(account.accountId())).willReturn(Optional.of(account));
    var accounts = new Accounts(accountStorage);

    // WHEN
    var result = accounts.withdrawMoneyFor(account.accountId(), oneGPB());

    // THEN
    verify(accountStorage).retrieve(account.accountId());
    verify(accountStorage).update(any());
    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  public void shouldReturnFailureIfNoAccountDuringWithdraw() {
    // GIVEN
    var accountId = AccountId.generate();
    var accountStorage = mock(AccountStorage.class);
    given(accountStorage.retrieve(accountId)).willReturn(Optional.empty());
    var accounts = new Accounts(accountStorage);

    // WHEN
    var result = accounts.withdrawMoneyFor(accountId, oneGPB());

    // THEN
    verify(accountStorage).retrieve(accountId);
    verify(accountStorage, never()).update(any());
    assertThat(result.isFailure()).isTrue();
    assertThat(result.failureInfo().get()).isInstanceOf(AccountNotFound.class);
  }

  @Test
  public void shouldReturnFailureIfBalanceIssueDuringWithdraw() {
    // GIVEN
    var account = Account.openAccountFor("Name", Currency.getInstance("GBP"));
    var accountStorage = mock(AccountStorage.class);
    given(accountStorage.retrieve(account.accountId())).willReturn(Optional.of(account));
    var accounts = new Accounts(accountStorage);

    // WHEN
    var result = accounts.withdrawMoneyFor(account.accountId(), oneGPB());

    // THEN
    verify(accountStorage).retrieve(account.accountId());
    verify(accountStorage, never()).update(any());
    assertThat(result.isFailure()).isTrue();
    assertThat(result.failureInfo().get()).isInstanceOf(AccountBalanceError.class);
  }

  @Test
  public void shouldReturnFailureIfStorageIssuesDuringDebitWithdraw() {
    // GIVEN
    var account = Account.openAccountFor("Name", Currency.getInstance("GBP"));
    account = account.debit(twoGPB()).successfulValue().orElseThrow();
    var accountStorage = mock(AccountStorage.class);
    given(accountStorage.update(any()))
        .willReturn(Failed.failed(AccountStorageError.OPTIMISTIC_LOCKING));
    given(accountStorage.retrieve(account.accountId())).willReturn(Optional.of(account));
    var accounts = new Accounts(accountStorage);

    // WHEN
    var result = accounts.withdrawMoneyFor(account.accountId(), oneGPB());

    // THEN
    verify(accountStorage).retrieve(account.accountId());
    verify(accountStorage).update(any());
    assertThat(result.isFailure()).isTrue();
    assertThat(result.failureInfo().get()).isInstanceOf(TooManyOperationWithinAccount.class);
  }
}
