plugins {
    id("java-library")
}

dependencies {
    api(project(":account_domain"))

    testImplementation(testFixtures(project(":account_domain")))
    testImplementation(testFixtures(project(":shared")))
}