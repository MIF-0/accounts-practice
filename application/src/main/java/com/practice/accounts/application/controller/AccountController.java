package com.practice.accounts.application.controller;

import com.practice.accounts.account.api.AccountCreateRequest;
import com.practice.accounts.account.api.Accounts;
import com.practice.accounts.application.configuration.Limitation;
import com.practice.accounts.shared.AccountId;
import com.practice.accounts.shared.RequestId;
import io.helidon.http.Status;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import java.util.Collections;
import java.util.Objects;

public class AccountController implements HttpService {

  private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

  private final Accounts accounts;
  private final Limitation limitation;

  public AccountController(Accounts accounts, Limitation limitation) {
    this.accounts = accounts;
    this.limitation = limitation;
  }

  @Override
  public void routing(HttpRules rules) {
    rules.post("/open-account", this::openAccount).get("/{id}", this::accountInfoHandler);
  }

  private void accountInfoHandler(ServerRequest request, ServerResponse response) {
    String id = request.path().pathParameters().get("id");
    var accountId = AccountId.from(id);
    var possibleAccount = accounts.retrieve(accountId);
    if (possibleAccount.isEmpty()) {
      response.status(Status.NOT_FOUND_404).send();
    } else {
      var account = possibleAccount.get();
      JsonObject returnObject =
          JSON.createObjectBuilder().add("account", account.toString()).build();
      response.send(returnObject);
    }
  }

  private void openAccount(ServerRequest request, ServerResponse response) {
    var requestBody = request.content().as(JsonObject.class);
    if (!requestBody.containsKey("name")) {
      JsonObject jsonErrorObject =
          JSON.createObjectBuilder()
              .add("error", "Not all data provided. Please provide [name]")
              .build();
      response.status(Status.BAD_REQUEST_400).send(jsonErrorObject);
      return;
    }
    var name = Objects.requireNonNull(requestBody.getString("name"));
    var currency = limitation.supportedCurrency();
    var result =
        accounts.createAccount(new AccountCreateRequest(RequestId.generate(), name, currency));

    if (result.isFailure()) {
      response.status(Status.BAD_REQUEST_400).send();
      return;
    }
    var accountId = result.successfulValue().orElseThrow().accountId();
    JsonObject returnObject =
        JSON.createObjectBuilder().add("account_id", accountId.value().toString()).build();
    response.send(returnObject);
  }
}
