package com.practice.accounts.account.api;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

import com.practice.accounts.account.api.error.AccountBalanceError;
import com.practice.accounts.account.api.error.AccountDuplicate;
import com.practice.accounts.account.api.error.AccountNotFound;
import com.practice.accounts.account.api.error.AccountsError;
import com.practice.accounts.account.api.error.TooManyOperationWithinAccount;
import com.practice.accounts.account.api.event.BalanceUpdated;
import com.practice.accounts.account.domain.AccountFactory;
import com.practice.accounts.account.external.AccountRepository;
import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.Failed;
import com.practice.accounts.shared.MoneyFactory;
import com.practice.accounts.shared.RequestId;
import com.practice.accounts.shared.Result;
import com.practice.accounts.shared.Success;
import java.util.Currency;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.III_Result;

/*
First 2 numbers is a result code converted from errors
Third number is a balance value
In this test we are expecting only BALANCE ERROR due to less money than needed
* **/
@JCStressTest
@Outcome(id = "200, 200, -1", expect = FORBIDDEN, desc = "One overwrite another.")
@Outcome(id = "200, 200, 2", expect = FORBIDDEN, desc = "One overwrite another.")
@Outcome(id = "200, 500, -1", expect = FORBIDDEN, desc = "Can't withdraw if no money")
@Outcome(id = "403, 200, 2", expect = ACCEPTABLE, desc = "Try to withdraw before debit")
@Outcome(id = "200, 200, 1", expect = ACCEPTABLE, desc = "Both updates.")
@State
public class AccountWithdrawSimpleStressTest implements AccountFactory, MoneyFactory {
  private static final int SUCCESS = 200;
  private static final int ACCOUNT_NOT_FOUND = 404;
  private static final int BALANCE_ERROR = 403;
  private static final int TOO_MANY_OPERATIONS = 500;
  private static final int DUPLiCATE = 999;
  private final InitialSetup initialSetup = new InitialSetup();

  @Actor
  public void actor1(III_Result r) {
    var withdraw = oneGPB();
    var result =
        initialSetup.accounts.withdrawMoneyFor(
            new WithdrawRequest(RequestId.generate(), initialSetup.accountId, withdraw));
    r.r1 = convertResult(result);
  }

  @Actor
  public void actor2(III_Result r) {
    var debit = twoGPB();
    var result =
        initialSetup.accounts.debitMoneyFor(
            new DebitRequest(RequestId.generate(), initialSetup.accountId, debit));
    r.r2 = convertResult(result);
  }

  @Arbiter
  public void arbiter(III_Result r) {
    r.r3 =
        initialSetup
            .accountRepository
            .retrieve(initialSetup.accountId)
            .orElseThrow()
            .balance()
            .value()
            .intValue();
  }

  private int convertResult(Result<BalanceUpdated, AccountsError> result) {
    return switch (result) {
      case Failed<BalanceUpdated, AccountsError> v -> mapError(v.failure());
      case Success<BalanceUpdated, AccountsError> v -> SUCCESS;
    };
  }

  private int mapError(AccountsError error) {
    return switch (error) {
      case AccountBalanceError accountBalanceError -> BALANCE_ERROR;
      case AccountDuplicate accountDuplicate -> DUPLiCATE;
      case AccountNotFound accountNotFound -> ACCOUNT_NOT_FOUND;
      case TooManyOperationWithinAccount tooManyOperationWithinAccount -> TOO_MANY_OPERATIONS;
    };
  }

  private static class InitialSetup {
    final Accounts accounts;
    final AccountId accountId;
    final AccountRepository accountRepository;

    public InitialSetup() {
      this.accountRepository = new AccountRepository();
      this.accounts = new Accounts(accountRepository);
      var result =
          accounts.createAccount(
              new AccountCreateRequest(RequestId.generate(), "Name", Currency.getInstance("GBP")));
      this.accountId = result.successfulValue().orElseThrow().accountId();
    }
  }
}
