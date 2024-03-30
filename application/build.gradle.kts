plugins {
    alias(libs.plugins.shadow)
    id("application")
    id("jvm-test-suite")
}

dependencies {
    implementation(platform(libs.helidon.bom))
    implementation(libs.bundles.webServer)

    implementation(project(":account_api"))
    implementation(project(":account_domain"))
    implementation(project(":account_external"))
    implementation(project(":transfer_api"))
    implementation(project(":transfer_domain"))
    implementation(project(":transfer_external"))

}

application {
    val name = "com.practice.accounts.application.App"
    mainClass.set(name)
}

testing {
    suites {
        val integrationTest by registering(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation(platform(libs.helidon.bom))
                implementation.bundle(libs.bundles.webServer)
                implementation(platform(libs.jUnit.bom))
                implementation.bundle(libs.bundles.testDependencies)
            }
        }
    }
}

tasks.shadowJar {
    archiveFileName = "accounts-app.${archiveExtension}"
}
