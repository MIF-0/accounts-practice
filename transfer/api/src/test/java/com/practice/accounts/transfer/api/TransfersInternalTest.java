package com.practice.accounts.transfer.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.practice.accounts.account.api.Accounts;
import com.practice.accounts.account.api.DebitRequest;
import com.practice.accounts.account.api.error.AccountBalanceError;
import com.practice.accounts.account.api.event.BalanceUpdated;
import com.practice.accounts.account.domain.error.BalanceError;
import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.Failed;
import com.practice.accounts.shared.Money;
import com.practice.accounts.shared.MoneyFactory;
import com.practice.accounts.shared.RequestId;
import com.practice.accounts.shared.Success;
import com.practice.accounts.transfer.domain.Receiver;
import com.practice.accounts.transfer.domain.Sender;
import com.practice.accounts.transfer.domain.TransferFactory;
import com.practice.accounts.transfer.domain.TransferStorage;
import com.practice.accounts.transfer.domain.WithdrawalService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

public class TransfersInternalTest implements MoneyFactory {

  @Test
  public void shouldSuccessfullyTopUp() {
    // GIVEN
    var sender = new Sender(AccountId.generate());
    var receiver = new Receiver(AccountId.generate());
    var transferStorage = mock(TransferStorage.class);
    var accounts = mock(Accounts.class);
    var transfers =
        new Transfers(
            new TransferFactory(AccountId.generate()),
            transferStorage,
            accounts,
            mock(WithdrawalService.class));
    var transferRequest =
        new InternalTransferRequest(RequestId.generate(), oneGPB(), sender, receiver);

    given(transferStorage.insert(any())).willReturn(Success.successVoid());
    given(transferStorage.update(any())).willReturn(Success.successVoid());
    given(accounts.withdrawMoneyFor(any()))
        .willReturn(new Success<>(new BalanceUpdated(receiver.accountId(), RequestId.generate())));
    given(accounts.debitMoneyFor(any()))
        .willReturn(new Success<>(new BalanceUpdated(receiver.accountId(), RequestId.generate())));

    // WHEN
    var result = transfers.internalTransfer(transferRequest);

    // THEN
    assertThat(result.isSuccess()).isTrue();
    verify(accounts, times(1)).withdrawMoneyFor(any());
    verify(accounts, times(1)).debitMoneyFor(any());
    verify(transferStorage, times(1)).insert(any());
    verify(transferStorage, times(2)).update(any());
  }

  @Test
  public void shouldFailIfWithdrawalFailed() {
    // GIVEN
    var sender = new Sender(AccountId.generate());
    var receiver = new Receiver(AccountId.generate());
    var transferStorage = mock(TransferStorage.class);
    var accounts = mock(Accounts.class);
    var transfers =
        new Transfers(
            new TransferFactory(AccountId.generate()),
            transferStorage,
            accounts,
            mock(WithdrawalService.class));
    var transferRequest =
        new InternalTransferRequest(RequestId.generate(), oneGPB(), sender, receiver);

    given(transferStorage.insert(any())).willReturn(Success.successVoid());
    given(transferStorage.update(any())).willReturn(Success.successVoid());
    given(accounts.withdrawMoneyFor(any()))
        .willReturn(Failed.failed(new AccountBalanceError(BalanceError.NOT_ENOUGH_MONEY)));
    given(accounts.debitMoneyFor(any()))
        .willReturn(new Success<>(new BalanceUpdated(receiver.accountId(), RequestId.generate())));

    // WHEN
    var result = transfers.internalTransfer(transferRequest);

    // THEN
    assertThat(result.isFailure()).isTrue();
    verify(accounts, never()).debitMoneyFor(any());
    verify(accounts, times(1)).withdrawMoneyFor(any());
    verify(transferStorage, times(1)).insert(any());
    verify(transferStorage, times(1)).update(any());
  }

  @Test
  public void shouldFailIfDebitFailed() {
    // GIVEN
    var sender = new Sender(AccountId.generate());
    var receiver = new Receiver(AccountId.generate());
    var transferStorage = mock(TransferStorage.class);
    var accounts = mock(Accounts.class);
    var transfers =
        new Transfers(
            new TransferFactory(AccountId.generate()),
            transferStorage,
            accounts,
            mock(WithdrawalService.class));
    var transferRequest =
        new InternalTransferRequest(RequestId.generate(), oneGPB(), sender, receiver);

    given(transferStorage.insert(any())).willReturn(Success.successVoid());
    given(transferStorage.update(any())).willReturn(Success.successVoid());
    given(accounts.withdrawMoneyFor(any()))
        .willReturn(new Success<>(new BalanceUpdated(receiver.accountId(), RequestId.generate())));
    given(accounts.debitMoneyFor(argThat(new DebitRequestMatcher(receiver.accountId(), oneGPB()))))
        .willReturn(Failed.failed(new AccountBalanceError(BalanceError.NOT_ENOUGH_MONEY)));
    given(accounts.debitMoneyFor(argThat(new DebitRequestMatcher(sender.accountId(), oneGPB()))))
        .willReturn(new Success<>(new BalanceUpdated(sender.accountId(), RequestId.generate())));

    // WHEN
    var result = transfers.internalTransfer(transferRequest);

    // THEN
    assertThat(result.isFailure()).isTrue();
    verify(accounts, times(1)).withdrawMoneyFor(any());
    verify(accounts, times(1))
        .debitMoneyFor(argThat(new DebitRequestMatcher(receiver.accountId(), oneGPB())));
    verify(accounts, times(1))
        .debitMoneyFor(argThat(new DebitRequestMatcher(sender.accountId(), oneGPB())));
    verify(transferStorage, times(1)).insert(any());
    verify(transferStorage, times(2)).update(any());
  }

  private static class DebitRequestMatcher implements ArgumentMatcher<DebitRequest> {

    private final AccountId accountId;
    private final Money money;

    public DebitRequestMatcher(AccountId accountId, Money money) {
      this.accountId = accountId;
      this.money = money;
    }

    @Override
    public boolean matches(DebitRequest debitRequest) {
      if (debitRequest == null) {
        return false;
      }
      return debitRequest.accountId().equals(accountId) && debitRequest.money().equals(money);
    }
  }
}
