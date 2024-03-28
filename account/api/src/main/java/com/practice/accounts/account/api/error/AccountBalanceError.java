package com.practice.accounts.account.api.error;

import com.practice.accounts.account.domain.error.BalanceError;

public record AccountBalanceError(BalanceError balanceError) implements AccountsError {}
