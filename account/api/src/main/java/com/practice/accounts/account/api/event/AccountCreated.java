package com.practice.accounts.account.api.event;

import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.RequestId;

public record AccountCreated(AccountId accountId, RequestId requestId) {}
