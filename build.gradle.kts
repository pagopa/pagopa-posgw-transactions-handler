import com.diffplug.spotless.kotlin.KtfmtStep

plugins {
  kotlin("jvm") version "2.3.21"
  kotlin("plugin.spring") version "2.3.21"
  id("org.springframework.boot") version "4.1.0"
  id("io.spring.dependency-management") version "1.1.7"
  id("org.graalvm.buildtools.native") version "1.1.1"
  id("com.diffplug.spotless") version "8.9.0"
}

group = "it.pagopa"

version = "0.0.1-SNAPSHOT"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

repositories {
  mavenCentral()
}

object Deps {
  const val ECS_LOGGING_VERSION = "1.8.0"
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
  implementation("tools.jackson.module:jackson-module-kotlin")

  // ECS logback encoder
  implementation("co.elastic.logging:logback-ecs-encoder:${Deps.ECS_LOGGING_VERSION}")

  testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
  }
}

spotless {
  kotlin {
    target("**/*.kt")
    targetExclude("build/**/*")
    toggleOffOn()

    ktfmt().kotlinlangStyle().configure {
      it.setTrailingCommaManagementStrategy(KtfmtStep.TrailingCommaManagementStrategy.NONE)
      it.setRemoveUnusedImports(true)
    }

    trimTrailingWhitespace()
    endWithNewline()
  }

  kotlinGradle {
    target("**/*.kts")
    targetExclude("build/**/*.kts")
    toggleOffOn()

    ktfmt().googleStyle()

    trimTrailingWhitespace()
    endWithNewline()
  }

  java {
    target("**/*.java")
    targetExclude("build/**/*")
    toggleOffOn()

    eclipse().configFile("eclipse-style.xml")
    removeUnusedImports()

    trimTrailingWhitespace()
    endWithNewline()
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

graalvmNative {
  binaries {
    named("main") {
      // Grant native access to suppress Java 22+ restricted method warnings
      // triggered when underlying frameworks (e.g., Reactor Netty) load C native libraries via JNI
      buildArgs.add("--enable-native-access=ALL-UNNAMED")
    }
  }
}

/**
 * Task used to expand application properties with build specific properties such as artifact name
 * and version
 */
tasks.processResources { filesMatching("application.properties") { expand(project.properties) } }
