package com.practice.accounts.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.practice.accounts.application.configuration.AppContextConfig;
import io.helidon.config.Config;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AccountTransferControllerTest {
  private static final Application application = createApplication();

  private static Application createApplication() {
    var config = Config.create();
    Config.global(config);
    var appContextConfig = new AppContextConfig();
    return new Application(config, appContextConfig);
  }

  @BeforeAll
  public static void startApp() {
    application.start();
  }

  @AfterAll
  public static void clean() {
    application.stop();
  }

  @Test
  public void shouldBeAbleToTopUp() throws URISyntaxException {
    // GIVEN
    try (var httpClient = HttpClient.newHttpClient()) {
      var accountId = openAccount(httpClient);

      var topUpRequest =
          HttpRequest.newBuilder()
              .uri(new URI("http://localhost:9080/account/" + accountId + "/transfer/top-up"))
              .headers("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString("{\"amount\":\"100\"}"))
              .build();

      // WHEN
      var response = httpClient.send(topUpRequest, HttpResponse.BodyHandlers.ofString());

      // THEN
      assertThat(response.statusCode()).isEqualTo(200);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void shouldBeAbleToTransfer() throws URISyntaxException {
    try (var httpClient = HttpClient.newHttpClient()) {
      // GIVEN
      var sender = openAccount(httpClient);
      var receiver = openAccount(httpClient);

      var topUpRequest =
          HttpRequest.newBuilder()
              .uri(new URI("http://localhost:9080/account/" + sender + "/transfer/top-up"))
              .headers("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString("{\"amount\":\"300\"}"))
              .build();
      var topUpResponse = httpClient.send(topUpRequest, HttpResponse.BodyHandlers.ofString());
      assertThat(topUpResponse.statusCode()).isEqualTo(200);

      var transferRequest =
          HttpRequest.newBuilder()
              .uri(new URI("http://localhost:9080/account/" + sender + "/transfer/internal"))
              .headers("Content-Type", "application/json")
              .POST(
                  HttpRequest.BodyPublishers.ofString(
                      "{\"amount\":\"300\", \"account_to\":\"" + receiver + "\"}"))
              .build();
      // WHEN
      var transferResponse = httpClient.send(transferRequest, HttpResponse.BodyHandlers.ofString());
      // THEN
      assertThat(transferResponse.statusCode()).isEqualTo(200);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private static String openAccount(HttpClient httpClient)
      throws URISyntaxException, IOException, InterruptedException {
    var openAccountRequest =
        HttpRequest.newBuilder()
            .uri(new URI("http://localhost:9080/account/open-account"))
            .headers("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"My name\"}"))
            .build();
    var openResponse = httpClient.send(openAccountRequest, HttpResponse.BodyHandlers.ofString());
    // {"account_id":"value"}
    var accountId =
        openResponse.body().replace("{", "").replace("}", "").replace("\"", "").split(":")[1];
    return accountId;
  }
}
