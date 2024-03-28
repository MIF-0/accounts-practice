package com.practice.accounts.account.api.error;

public sealed interface AccountsError
    permits AccountBalanceError, AccountDuplicate, AccountNotFound, TooManyOperationWithinAccount {}
