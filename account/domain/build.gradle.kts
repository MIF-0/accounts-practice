plugins {
    id("java-library")
}

dependencies {
    api(project(":shared"))

    testImplementation(testFixtures(project(":shared")))
}