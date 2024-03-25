plugins {
    alias(libs.plugins.spotless)
    id("java")
}

allprojects {
    project.layout.buildDirectory.set(File(rootProject.projectDir, "build/" + project.name + "/build"))
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "java-test-fixtures")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "java")

    group = "com.company.accounts"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    val libs = rootProject.libs

    dependencies {
        implementation(libs.bundles.logDependencies)
        testImplementation(platform(libs.jUnit.bom))
        testImplementation(libs.bundles.testDependencies)
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    spotless {
        java {
            importOrder()
            removeUnusedImports()

            // apply a specific flavor of google-java-format
            googleJavaFormat().reflowLongStrings()
            custom("Refuse wildcard imports") {
                // Wildcard imports can't be resolved by spotless itself.
                // This will require the developer themselves to adhere to best practices.
                val regex = Regex("import .*\\*;")
                if (regex.containsMatchIn(it)) {
                    throw AssertionError("Do not use wildcard imports.  'spotlessApply' cannot resolve this issue.")
                }
                it
            }
        }
    }
}

spotless {
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }

    format("misc") {
        target("*.md", ".gitignore", "*.toml")
        trimTrailingWhitespace()
        endWithNewline()
    }
}
