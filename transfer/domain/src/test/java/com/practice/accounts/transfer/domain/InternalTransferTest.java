package com.practice.accounts.transfer.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.MoneyFactory;
import com.practice.accounts.shared.Version;
import org.junit.jupiter.api.Test;

public class InternalTransferTest implements MoneyFactory {

  @Test
  public void shouldMarkStatusAsWithdrawDoneAndUpdateVersion() {
    // GIVEN
    var transferFactory = new TransferFactory(AccountId.generate());
    var transfer = transferFactory.newTopUp(oneGPB(), new Receiver(AccountId.generate()));

    // WHEN
    var updatedTransfer =
        (InternalTransfer) transfer.withdrawFinished().successfulValue().orElseThrow();

    // THEN
    assertThat(updatedTransfer.status()).isEqualTo(InternalTransfer.Status.WITHDRAW_DONE);
    assertThat(updatedTransfer.version()).isEqualTo(Version.createFirstVersion().next());
  }

  @Test
  public void shouldFailIfAlreadyWithdrawFinished() {
    // GIVEN
    var transferFactory = new TransferFactory(AccountId.generate());
    Transfer transfer = transferFactory.newTopUp(oneGPB(), new Receiver(AccountId.generate()));
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
    Transfer transfer = transferFactory.newTopUp(oneGPB(), new Receiver(AccountId.generate()));
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
    var transfer = transferFactory.newTopUp(oneGPB(), new Receiver(AccountId.generate()));
    var updatedTransfer = transfer.withdrawFinished().successfulValue().orElseThrow();

    // WHEN
    var finished = (InternalTransfer) updatedTransfer.markAsDone().successfulValue().orElseThrow();

    // THEN
    assertThat(finished.status()).isEqualTo(InternalTransfer.Status.FULLY_DONE);
    assertThat(finished.version()).isEqualTo(Version.createFirstVersion().next().next());
  }

  @Test
  public void shouldFailIfAlreadyDone() {
    // GIVEN
    var transferFactory = new TransferFactory(AccountId.generate());
    Transfer transfer = transferFactory.newTopUp(oneGPB(), new Receiver(AccountId.generate()));
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
    var transfer = transferFactory.newTopUp(oneGPB(), new Receiver(AccountId.generate()));

    // WHEN
    var updatedTransfer = transfer.markAsDone();

    // THEN
    assertThat(updatedTransfer.isFailure()).isTrue();
  }

  @Test
  public void shouldMarkStatusAsFailed() {
    // GIVEN
    var transferFactory = new TransferFactory(AccountId.generate());
    var transfer = transferFactory.newTopUp(oneGPB(), new Receiver(AccountId.generate()));
    var updatedTransfer = transfer.withdrawFinished().successfulValue().orElseThrow();

    // WHEN
    var finished =
        (InternalTransfer) updatedTransfer.markAsFailed().successfulValue().orElseThrow();

    // THEN
    assertThat(finished.status()).isEqualTo(InternalTransfer.Status.FULLY_DONE);
    assertThat(finished.version()).isEqualTo(Version.createFirstVersion().next().next());
  }

  @Test
  public void shouldFailIToMarkFailedIfAlreadyDone() {
    // GIVEN
    var transferFactory = new TransferFactory(AccountId.generate());
    Transfer transfer = transferFactory.newTopUp(oneGPB(), new Receiver(AccountId.generate()));
    transfer = transfer.withdrawFinished().successfulValue().orElseThrow();
    transfer = transfer.markAsDone().successfulValue().orElseThrow();

    // WHEN
    var updatedTransfer = transfer.markAsFailed();

    // THEN
    assertThat(updatedTransfer.isFailure()).isTrue();
  }
}
