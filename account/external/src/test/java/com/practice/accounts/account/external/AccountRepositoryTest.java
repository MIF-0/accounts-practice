package com.practice.accounts.account.external;

import static org.assertj.core.api.Assertions.assertThat;

import com.practice.accounts.account.domain.AccountFactory;
import com.practice.accounts.account.domain.error.AccountStorageError;
import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.MoneyFactory;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class AccountRepositoryTest implements MoneyFactory, AccountFactory {

  @Test
  public void shouldReturnSuccessIfNoAccountExist() {
    // GIVEN
    var account = newGBPAccount();
    var repo = new AccountRepository();

    // WHEN
    var result = repo.insert(account);

    // THEN
    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  public void shouldReturnDuplicateIfAccountAlreadyExist() {
    // GIVEN
    var account = newGBPAccount();
    var repo = new AccountRepository();
    repo.insert(account);

    // WHEN
    var result = repo.insert(account);

    // THEN
    assertThat(result.isFailure()).isTrue();
    assertThat(result.failureInfo().get()).isEqualTo(AccountStorageError.DUPLICATE);
  }

  @Test
  public void shouldReturnEmptyIfNoAccountExist() {
    // GIVEN
    var repo = new AccountRepository();

    // WHEN
    var result = repo.retrieve(AccountId.generate());

    // THEN
    assertThat(result).isEmpty();
  }

  @Test
  public void shouldReturnValueIfAccountExist() {
    // GIVEN
    var account = newGBPAccount();
    var repo = new AccountRepository();
    repo.insert(account);

    // WHEN
    var result = repo.retrieve(account.id());

    // THEN
    assertThat(result).isEqualTo(Optional.of(account));
  }

  @Test
  public void shouldReturnNotFoundForUpdateIfNoAccount() {
    // GIVEN
    var account = newGBPAccount();
    var repo = new AccountRepository();

    // WHEN
    var result = repo.update(account);

    // THEN
    assertThat(result.isFailure()).isTrue();
    assertThat(result.failureInfo()).isEqualTo(Optional.of(AccountStorageError.KEY_NOT_FOUND));
  }

  @Test
  public void shouldReturnSuccessForUpdateIfVersionIsSameAccount() {
    // GIVEN
    var account = newGBPAccount();
    var repo = new AccountRepository();
    repo.insert(account);

    // WHEN
    var result = repo.update(account);

    // THEN
    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  public void shouldReturnOptimisticLockingErrorForUpdateIfVersionIsWrongAccount() {
    // GIVEN
    var account = newGBPAccount();
    var repo = new AccountRepository();
    repo.insert(account);

    var newAccount = account.debit(oneGPB()).successfulValue().orElseThrow();
    newAccount = newAccount.debit(oneGPB()).successfulValue().orElseThrow();

    // WHEN
    var result = repo.update(newAccount);

    // THEN
    assertThat(result.isFailure()).isTrue();
    assertThat(result.failureInfo()).isEqualTo(Optional.of(AccountStorageError.OPTIMISTIC_LOCKING));
  }

  @Test
  public void shouldReturnSuccessForUpdateIfNextVersion() {
    // GIVEN
    var account = newGBPAccount();
    var repo = new AccountRepository();
    repo.insert(account);

    var newAccount = account.debit(oneGPB()).successfulValue().orElseThrow();

    // WHEN
    var result = repo.update(newAccount);

    // THEN
    assertThat(result.isSuccess()).isTrue();
  }
}
