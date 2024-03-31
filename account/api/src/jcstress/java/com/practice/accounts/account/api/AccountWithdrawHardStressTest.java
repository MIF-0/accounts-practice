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
import org.openjdk.jcstress.infra.results.IIII_Result;

@JCStressTest
@Outcome(id = "200, 200, 200, -1", expect = FORBIDDEN, desc = "One overwrite another.")
@Outcome(id = "200, 200, 200, 2", expect = FORBIDDEN, desc = "One overwrite another.")
@Outcome(id = "200, 200, 200, 10", expect = FORBIDDEN, desc = "One overwrite another.")
@Outcome(id = "200, 200, 200, 12", expect = FORBIDDEN, desc = "One overwrite another.")
@Outcome(id = "200, 200, 200, 9", expect = FORBIDDEN, desc = "One overwrite another.")
@Outcome(id = "200, 200, 200, 1", expect = FORBIDDEN, desc = "One overwrite another.")
@Outcome(
    id = "200, 200, 500, 1",
    expect = ACCEPTABLE,
    desc = "Only two updates (2-1=1). Optimistic lock")
@Outcome(
    id = "200, 500, 200, 9",
    expect = ACCEPTABLE,
    desc = "Only two updates(10-1=0). Optimistic lock")
@Outcome(
    id = "500, 200, 200, 12",
    expect = ACCEPTABLE,
    desc = "Only two update(10+2=12). Optimistic lock")
@Outcome(
    id = "403, 200, 200, 12",
    expect = ACCEPTABLE,
    desc = "Only two updates(2+10=12). Because withdraw was done first")
@Outcome(
    id = "403, 500, 200, 10",
    expect = ACCEPTABLE,
    desc = "Only one update(10). Because Optimistic lock + withdraw was done first")
@Outcome(
    id = "403, 200, 500, 2",
    expect = ACCEPTABLE,
    desc = "Only one update(2). Because Optimistic lock + withdraw was done first")
@Outcome(id = "200, 200, 200, 11", expect = ACCEPTABLE, desc = "All Updates(-1+2+10=11)")
@State
public class AccountWithdrawHardStressTest implements AccountFactory, MoneyFactory {
  private static final int SUCCESS = 200;
  private static final int ACCOUNT_NOT_FOUND = 404;
  private static final int BALANCE_ERROR = 403;
  private static final int TOO_MANY_OPERATIONS = 500;
  private static final int DUPLiCATE = 999;
  private final InitialSetup initialSetup = new InitialSetup();

  @Actor
  public void actor1(IIII_Result r) {
    var withdraw = oneGPB();
    var result =
        initialSetup.accounts.withdrawMoneyFor(
            new WithdrawRequest(RequestId.generate(), initialSetup.accountId, withdraw));
    r.r1 = convertResult(result);
  }

  @Actor
  public void actor2(IIII_Result r) {
    var debit = twoGPB();
    var result =
        initialSetup.accounts.debitMoneyFor(
            new DebitRequest(RequestId.generate(), initialSetup.accountId, debit));
    r.r2 = convertResult(result);
  }

  @Actor
  public void actor3(IIII_Result r) {
    var debit = tenGPB();
    var result =
        initialSetup.accounts.debitMoneyFor(
            new DebitRequest(RequestId.generate(), initialSetup.accountId, debit));
    r.r3 = convertResult(result);
  }

  @Arbiter
  public void arbiter(IIII_Result r) {
    r.r4 =
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
      case Success<BalanceUpdated, AccountsError> ignored -> SUCCESS;
    };
  }

  private int mapError(AccountsError error) {
    return switch (error) {
      case AccountBalanceError ignored -> BALANCE_ERROR;
      case AccountDuplicate ignored -> DUPLiCATE;
      case AccountNotFound ignored -> ACCOUNT_NOT_FOUND;
      case TooManyOperationWithinAccount ignored -> TOO_MANY_OPERATIONS;
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

/*
First 3 numbers is a result code converted from errors
Forth number is a balance value
In this test we are expecting TOO_MANY_OPERATIONS error due to optimistic locking a
nd BALANCE ERROR due to less money than needed
* **/
