plugins {
    id("java-library")
    id("io.github.reyerizo.gradle.jcstress") version "0.8.15"
}

dependencies {
    api(project(":account_domain"))

    testImplementation(testFixtures(project(":shared")))
    testImplementation("org.openjdk.jcstress:jcstress-core:0.16")
    jcstress(project(":account_external"))
    jcstress(testFixtures(project(":account_domain")))
    jcstress(testFixtures(project(":shared")))
}