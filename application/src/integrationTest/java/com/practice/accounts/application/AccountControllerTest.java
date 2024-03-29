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

public class AccountControllerTest {
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
  public void shouldBeAbleToOpenAccount() throws URISyntaxException {
    // GIVEN
    var request =
        HttpRequest.newBuilder()
            .uri(new URI("http://localhost:9080/account/open-account"))
            .headers("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"My name\"}"))
            .build();

    // WHEN
    try (var httpClient = HttpClient.newHttpClient()) {
      var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      // THEN
      assertThat(response.statusCode()).isEqualTo(200);
      assertThat(response.body().isEmpty()).isFalse();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void shouldBeAbleToRetrieveAccount() throws URISyntaxException {
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
      var getRequest =
          HttpRequest.newBuilder()
              .uri(new URI("http://localhost:9080/account/" + accountId))
              .headers("Content-Type", "application/json")
              .GET()
              .build();

      // WHEN
      var getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

      // THEN
      assertThat(getResponse.statusCode()).isEqualTo(200);
      assertThat(getResponse.body()).contains("My name");
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
