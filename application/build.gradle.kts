plugins {
    alias(libs.plugins.shadow)
    id("application")
}

dependencies {
    implementation(platform(libs.helidon.bom))
    implementation(libs.bundles.webServer)
}

application {
    val name = "com.practice.accounts.application.App"
    mainClass.set(name)
}

tasks.shadowJar {
    archiveFileName = "accounts-app.${archiveExtension}"
}
