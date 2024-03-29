package com.practice.accounts.application;

import com.practice.accounts.application.configuration.AppContextConfig;
import io.helidon.config.Config;

public class Main {

  public static void main(String[] args) {
    var config = Config.create();
    Config.global(config);
    var appContextConfig = new AppContextConfig();
    var app = new Application(config, appContextConfig);
    app.start();
  }
}
