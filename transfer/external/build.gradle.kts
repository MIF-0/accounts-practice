plugins {
    id("java-library")
}

dependencies {
    api(project(":transfer_domain"))

    testImplementation(testFixtures(project(":shared")))
}