package com.practice.accounts;

import io.helidon.config.Config;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
  private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) {
    var config = Config.create();
    Config.global(config);

    var server =
        WebServer.builder().config(config.get("server")).routing(App::routing).build().start();

    LOGGER.info("WEB server is up! http://localhost:" + server.port());
  }

  static void routing(HttpRouting.Builder routing) {
    routing.get("/hello", (req, res) -> res.send("Hello World!"));
  }
}
