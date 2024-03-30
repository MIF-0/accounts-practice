package com.practice.accounts.transfer.api;

import com.practice.accounts.shared.Money;
import com.practice.accounts.shared.RequestId;
import com.practice.accounts.transfer.domain.Receiver;

public record TopUpRequest(RequestId requestId, Money money, Receiver receiver) {}
