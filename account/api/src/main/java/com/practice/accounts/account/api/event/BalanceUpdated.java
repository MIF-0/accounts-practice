package com.practice.accounts.account.api.event;

import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.RequestId;

public record BalanceUpdated(AccountId accountId, RequestId requestId) {}
