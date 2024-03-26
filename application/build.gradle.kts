plugins {
    alias(libs.plugins.shadow)
    id("application")
}

dependencies {
    implementation(platform(libs.helidon.bom))
    implementation(libs.bundles.webServer)
}