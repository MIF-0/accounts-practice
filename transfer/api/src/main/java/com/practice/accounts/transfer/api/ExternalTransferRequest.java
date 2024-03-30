package com.practice.accounts.transfer.api;

import com.practice.accounts.shared.Money;
import com.practice.accounts.shared.RequestId;
import com.practice.accounts.transfer.domain.Sender;
import com.practice.accounts.transfer.domain.WithdrawalService.Address;

public record ExternalTransferRequest(
    RequestId requestId, Money money, Sender sender, Address receiver) {}
