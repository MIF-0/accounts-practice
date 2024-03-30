package com.practice.accounts.application.controller;

import com.practice.accounts.application.configuration.Limitation;
import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.Failed;
import com.practice.accounts.shared.Money;
import com.practice.accounts.shared.RequestId;
import com.practice.accounts.shared.Result;
import com.practice.accounts.shared.Success;
import com.practice.accounts.transfer.api.InternalTransferRequest;
import com.practice.accounts.transfer.api.TopUpRequest;
import com.practice.accounts.transfer.api.TransferError;
import com.practice.accounts.transfer.api.Transfers;
import com.practice.accounts.transfer.domain.Receiver;
import com.practice.accounts.transfer.domain.Sender;
import io.helidon.http.Status;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Supplier;

public class AccountTransferController implements HttpService {

  private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

  private final Transfers transfers;
  private final Limitation limitation;

  public AccountTransferController(Transfers transfers, Limitation limitation) {
    this.transfers = transfers;
    this.limitation = limitation;
  }

  @Override
  public void routing(HttpRules rules) {
    rules.post("/top-up", this::topUp);
    rules.post("/internal", this::internalTransferTo);
  }

  private void topUp(ServerRequest request, ServerResponse response) {
    String id = request.path().pathParameters().get("id");
    var requestBody = request.content().as(JsonObject.class);
    if (!requestBody.containsKey("amount")) {
      sendBadRequest("Not all data provided. Please provide [amount]", response);
      return;
    }
    var amountStr = Objects.requireNonNull(requestBody.getString("amount"));
    var currency = limitation.supportedCurrency();
    var amount = new BigDecimal(amountStr).setScale(2, RoundingMode.HALF_UP);
    var possibleMoney = Money.money(amount, currency);

    if (possibleMoney.isFailure()) {
      sendBadRequest("Wrong amount", response);
      return;
    }

    var accountId = AccountId.from(id);
    var topUpRequest =
        new TopUpRequest(
            RequestId.generate(), possibleMoney.successfulValue().get(), new Receiver(accountId));
    var result = withRetry(() -> transfers.topUp(topUpRequest));
    switch (result) {
      case Failed<Void, TransferError> v -> {
        response.status(Status.INTERNAL_SERVER_ERROR_500).send();
      }
      case Success<Void, TransferError> v -> {
        response.status(Status.OK_200).send();
      }
    }
  }

  private void internalTransferTo(ServerRequest request, ServerResponse response) {
    String accountIdStr = request.path().pathParameters().get("id");
    var requestBody = request.content().as(JsonObject.class);
    if (!requestBody.containsKey("amount") || !requestBody.containsKey("account_to")) {
      sendBadRequest("Not all data provided. Please provide [amount]", response);
      return;
    }
    var amountStr = Objects.requireNonNull(requestBody.getString("amount"));
    var currency = limitation.supportedCurrency();
    var amount = new BigDecimal(amountStr).setScale(2, RoundingMode.HALF_UP);
    var possibleMoney = Money.money(amount, currency);

    if (possibleMoney.isFailure()) {
      sendBadRequest("Wrong amount", response);
      return;
    }

    String accountTo = Objects.requireNonNull(requestBody.getString("account_to"));

    var sender = new Sender(AccountId.from(accountIdStr));
    var receiver = new Receiver(AccountId.from(accountTo));
    var internalTransfer =
        new InternalTransferRequest(
            RequestId.generate(), possibleMoney.successfulValue().get(), sender, receiver);
    var result = withRetry(() -> transfers.internalTransfer(internalTransfer));
    switch (result) {
      case Failed<Void, TransferError> v -> {
        response.status(Status.INTERNAL_SERVER_ERROR_500).send();
      }
      case Success<Void, TransferError> v -> {
        response.status(Status.OK_200).send();
      }
    }
  }

  private Result<Void, TransferError> withRetry(Supplier<Result<Void, TransferError>> task) {
    var numberOfRetry = 5;
    var result = task.get();
    for (var i = 0; i < numberOfRetry; i++) {
      switch (result) {
        case Failed<Void, TransferError> failed -> {
          if ((failed.failure() != TransferError.TOO_MANY_OPERATION)) {
            return failed;
          }
        }
        case Success<Void, TransferError> success -> {
          return success;
        }
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      result = task.get();
    }
    return result;
  }

  private void sendBadRequest(String error, ServerResponse response) {
    JsonObject jsonErrorObject = JSON.createObjectBuilder().add("error", error).build();
    response.status(Status.BAD_REQUEST_400).send(jsonErrorObject);
  }
}
