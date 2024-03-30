package com.practice.accounts.transfer.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.MoneyFactory;
import com.practice.accounts.shared.Version;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class ExternalTransferTest implements MoneyFactory {

  @Test
  public void shouldMarkStatusAsWithdrawDoneAndUpdateVersion() {
    // GIVEN
    var transferFactory = new TransferFactory(AccountId.generate());
    var transfer =
        transferFactory.newExternalTransfer(
            oneGPB(),
            new WithdrawalService.Address(UUID.randomUUID().toString()),
            new Sender(AccountId.generate()));

    // WHEN
    var updatedTransfer =
        (ExternalTransfer) transfer.withdrawFinished().successfulValue().orElseThrow();

    // THEN
    assertThat(updatedTransfer.status()).isEqualTo(ExternalTransfer.Status.WITHDRAW_DONE);
    assertThat(updatedTransfer.version()).isEqualTo(Version.createFirstVersion().next());
  }

  @Test
  public void shouldFailIfAlreadyWithdrawFinished() {
    // GIVEN
    var transferFactory = new TransferFactory(AccountId.generate());
    Transfer transfer =
        transferFactory.newExternalTransfer(
            oneGPB(),
            new WithdrawalService.Address(UUID.randomUUID().toString()),
            new Sender(AccountId.generate()));
    transfer = transfer.withdrawFinished().successfulValue().orElseThrow();

    // WHEN
    var updatedTransfer = transfer.withdrawFinished();

    // THEN
    assertThat(updatedTransfer.isFailure()).isTrue();
  }

  @Test
  public void shouldFailIfAlreadyFinished() {
    // GIVEN
    var transferFactory = new TransferFactory(AccountId.generate());
    Transfer transfer =
        transferFactory.newExternalTransfer(
            oneGPB(),
            new WithdrawalService.Address(UUID.randomUUID().toString()),
            new Sender(AccountId.generate()));
    transfer = transfer.withdrawFinished().successfulValue().orElseThrow();
    transfer = transfer.markAsDone().successfulValue().orElseThrow();

    // WHEN
    var updatedTransfer = transfer.withdrawFinished();

    // THEN
    assertThat(updatedTransfer.isFailure()).isTrue();
  }

  @Test
  public void shouldMarkStatusAsFullyDoneAndUpdateVersion() {
    // GIVEN
    var transferFactory = new TransferFactory(AccountId.generate());
    var transfer =
        transferFactory.newExternalTransfer(
            oneGPB(),
            new WithdrawalService.Address(UUID.randomUUID().toString()),
            new Sender(AccountId.generate()));
    var updatedTransfer = transfer.withdrawFinished().successfulValue().orElseThrow();

    // WHEN
    var doneTransfer =
        (ExternalTransfer) updatedTransfer.markAsDone().successfulValue().orElseThrow();

    // THEN
    assertThat(doneTransfer.status()).isEqualTo(ExternalTransfer.Status.FULLY_DONE);
    assertThat(doneTransfer.version()).isEqualTo(Version.createFirstVersion().next().next());
  }

  @Test
  public void shouldFailIfAlreadyDone() {
    // GIVEN
    var transferFactory = new TransferFactory(AccountId.generate());
    Transfer transfer =
        transferFactory.newExternalTransfer(
            oneGPB(),
            new WithdrawalService.Address(UUID.randomUUID().toString()),
            new Sender(AccountId.generate()));
    transfer = transfer.withdrawFinished().successfulValue().orElseThrow();
    transfer = transfer.markAsDone().successfulValue().orElseThrow();

    // WHEN
    var updatedTransfer = transfer.markAsDone();

    // THEN
    assertThat(updatedTransfer.isFailure()).isTrue();
  }

  @Test
  public void shouldNotMarkAsFullyDoneIfNew() {
    // GIVEN
    var transferFactory = new TransferFactory(AccountId.generate());
    var transfer =
        transferFactory.newExternalTransfer(
            oneGPB(),
            new WithdrawalService.Address(UUID.randomUUID().toString()),
            new Sender(AccountId.generate()));

    // WHEN
    var updatedTransfer = transfer.markAsDone();

    // THEN
    assertThat(updatedTransfer.isFailure()).isTrue();
  }

  @Test
  public void shouldMarkStatusAsFailed() {
    // GIVEN
    var transferFactory = new TransferFactory(AccountId.generate());
    var transfer =
        transferFactory.newExternalTransfer(
            oneGPB(),
            new WithdrawalService.Address(UUID.randomUUID().toString()),
            new Sender(AccountId.generate()));
    var updatedTransfer = transfer.withdrawFinished().successfulValue().orElseThrow();

    // WHEN
    var failedTransfer =
        (ExternalTransfer) updatedTransfer.markAsFailed().successfulValue().orElseThrow();

    // THEN
    assertThat(failedTransfer.status()).isEqualTo(ExternalTransfer.Status.FULLY_DONE);
    assertThat(failedTransfer.version()).isEqualTo(Version.createFirstVersion().next().next());
  }

  @Test
  public void shouldFailIToMarkFailedIfAlreadyDone() {
    // GIVEN
    var transferFactory = new TransferFactory(AccountId.generate());
    Transfer transfer =
        transferFactory.newExternalTransfer(
            oneGPB(),
            new WithdrawalService.Address(UUID.randomUUID().toString()),
            new Sender(AccountId.generate()));
    transfer = transfer.withdrawFinished().successfulValue().orElseThrow();
    transfer = transfer.markAsDone().successfulValue().orElseThrow();

    // WHEN
    var updatedTransfer = transfer.markAsFailed();

    // THEN
    assertThat(updatedTransfer.isFailure()).isTrue();
  }
}
