plugins {
    id("java-library")
}

dependencies {
    implementation(project(":account_api"))
    implementation(project(":transfer_domain"))

    testImplementation(testFixtures(project(":shared")))
}