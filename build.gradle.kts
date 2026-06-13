import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    id("java")
    // IntelliJ Platform Gradle Plugin 2.x (https://github.com/JetBrains/intellij-platform-gradle-plugin)
    id("org.jetbrains.intellij.platform") version "2.16.0"
}

group = "com.github.refal5lambda"
version = "0.0.2"

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
            // Always verify the supported floor (2024.3 == sinceBuild). This is reused from the
            // compile dependency above, so it needs no extra download and runs even offline.
            create(IntelliJPlatformType.IntellijIdeaCommunity, "2024.3")
            // Widen coverage to the currently-recommended releases (latest patches of recent
            // majors, up to the newest). Because the plugin leaves untilBuild open, it claims
            // forward compatibility, and this is what actually checks that claim against newer
            // IDEs. The list is fetched from JetBrains' product-releases metadata and each IDE is
            // downloaded at verification time, so this requires network: it runs in CI
            // (.github/workflows/build.yml -> ./gradlew verifyPlugin), not in an offline sandbox.
            recommended()
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
