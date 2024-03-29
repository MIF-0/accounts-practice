package com.practice.accounts.application;

import com.practice.accounts.application.configuration.AppContextConfig;
import com.practice.accounts.application.controller.AccountController;
import io.helidon.config.Config;
import io.helidon.http.media.jsonp.JsonpSupport;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
  private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) {
    var config = Config.create();
    Config.global(config);
    var appContextConfig = new AppContextConfig();
    initialSetup(appContextConfig);
    var server =
        WebServer.builder()
            .config(config.get("server"))
            .routing(App.routing(appContextConfig))
            .mediaContext(
                it ->
                    it.mediaSupportsDiscoverServices(false)
                        .addMediaSupport(JsonpSupport.create())
                        .build())
            .build()
            .start();

    LOGGER.info("WEB server is up! http://localhost:" + server.port());
  }

  static void initialSetup(AppContextConfig config) {
    var accountRepo = config.accountContextConfig().accountStorage();
    var companyAccount = config.limitation().companyAccount();
    var result = accountRepo.insert(companyAccount);
    if (result.isFailure()) {
      LOGGER.error("Were not able to create company account");
    } else {
      LOGGER.info("company account created");
    }
  }

  static Consumer<HttpRouting.Builder> routing(AppContextConfig config) {
    var accountsConfig = config.accountContextConfig();
    var limitation = config.limitation();
    return routing -> {
      routing
          .get("/hello", (req, res) -> res.send("Hello World!"))
          .register("/account", new AccountController(accountsConfig.accounts(), limitation));
    };
  }
}
