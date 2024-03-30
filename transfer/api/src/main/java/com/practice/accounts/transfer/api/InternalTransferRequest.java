package com.practice.accounts.transfer.api;

import com.practice.accounts.shared.Money;
import com.practice.accounts.shared.RequestId;
import com.practice.accounts.transfer.domain.Receiver;
import com.practice.accounts.transfer.domain.Sender;

public record InternalTransferRequest(
    RequestId requestId, Money money, Sender sender, Receiver receiver) {}
