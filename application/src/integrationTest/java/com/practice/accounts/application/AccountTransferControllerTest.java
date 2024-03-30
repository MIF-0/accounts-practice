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
    var openAccountRequest =
        HttpRequest.newBuilder()
            .uri(new URI("http://localhost:9080/account/open-account"))
            .headers("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"My name\"}"))
            .build();
    try (var httpClient = HttpClient.newHttpClient()) {
      var openResponse = httpClient.send(openAccountRequest, HttpResponse.BodyHandlers.ofString());
      // {"account_id":"value"}
      var accountId =
          openResponse.body().replace("{", "").replace("}", "").replace("\"", "").split(":")[1];

      var topUpRequest =
          HttpRequest.newBuilder()
              .uri(new URI("http://localhost:9080/account/" + accountId + "/transfer/top-up"))
              .headers("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString("{\"amount\":\"100\"}"))
              .build();

      // WHEN
      var getResponse = httpClient.send(topUpRequest, HttpResponse.BodyHandlers.ofString());

      // THEN
      assertThat(getResponse.statusCode()).isEqualTo(200);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
