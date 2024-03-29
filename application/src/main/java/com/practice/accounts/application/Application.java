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

public class Application {
  private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

  private final Config config;
  private final AppContextConfig appContextConfig;
  private final WebServer webServer;

  public Application(Config config, AppContextConfig appContextConfig) {
    this.config = config;
    this.appContextConfig = appContextConfig;
    this.webServer =
        WebServer.builder()
            .config(config.get("server"))
            .routing(Application.routing(appContextConfig))
            .mediaContext(
                it ->
                    it.mediaSupportsDiscoverServices(false)
                        .addMediaSupport(JsonpSupport.create())
                        .build())
            .build();
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

  public void start() {
    initialSetup(appContextConfig);
    this.webServer.start();
    LOGGER.info("WEB server is up! http://localhost:" + webServer.port());
  }

  public void stop() {
    this.webServer.stop();
    LOGGER.info("WEB server stopped");
  }

  private void initialSetup(AppContextConfig config) {
    var accountRepo = config.accountContextConfig().accountStorage();
    var companyAccount = config.limitation().companyAccount();
    var result = accountRepo.insert(companyAccount);
    if (result.isFailure()) {
      LOGGER.error("Were not able to create company account");
    } else {
      LOGGER.info("company account created");
    }
  }
}
