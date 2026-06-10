import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    id("java")
    // IntelliJ Platform Gradle Plugin 2.x (https://github.com/JetBrains/intellij-platform-gradle-plugin)
    id("org.jetbrains.intellij.platform") version "2.16.0"
}

group = "com.github.refal5lambda"
version = "0.0.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // Build/compile against IntelliJ IDEA Community 2024.3 (build 243).
        // Only long-stable platform APIs are used, so the plugin also loads on newer IDEs
        // (2025.x and later) because untilBuild is left open below.
        intellijIdeaCommunity("2024.3")

        // Platform test fixtures (ParsingTestCase, BasePlatformTestCase, ...).
        testFramework(TestFrameworkType.Platform)
    }

    // JUnit 4 runs both the pure scanner tests and the JUnit3-based platform test case.
    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    instrumentCode = false
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "243"
            // No upper bound: the plugin only uses long-stable APIs, so it should keep loading on
            // future IDEs. (The Plugin Verifier rejects a "magic" untilBuild like 999.*; omitting it
            // is the recommended way to stay compatible with all future versions.)
            untilBuild = provider { null }
        }
    }

    pluginVerification {
        ides {
            // Verify binary compatibility against the supported baseline IDE (reused from the
            // compile dependency above, so this needs no extra download). Add recommended() or a
            // newer build here to widen coverage.
            create(IntelliJPlatformType.IntellijIdeaCommunity, "2024.3")
        }
    }
}

java {
    toolchain {
        // IntelliJ 2024.3+ runs on JDK 21.
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.test {
    useJUnit()
    workingDir = project.projectDir   // so getTestDataPath() ("src/test/testData") resolves
}
