import com.diffplug.spotless.kotlin.KtfmtStep

plugins {
  kotlin("jvm") version "2.3.21"
  kotlin("plugin.spring") version "2.3.21"
  id("org.springframework.boot") version "4.1.0"
  id("io.spring.dependency-management") version "1.1.7"
  id("org.graalvm.buildtools.native") version "1.1.1"
  id("com.diffplug.spotless") version "8.9.0"
  id("com.dipien.semantic-version") version "2.0.0" apply false
  id("org.openapi.generator") version "7.25.0"
}

group = "it.pagopa"

version = "0.0.5"

apply(plugin = "com.dipien.semantic-version")

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
  const val SWAGGER_ANNOTATIONS_VERSION = "2.2.31"
  const val JACKSON_DATABIND_NULLABLE_VERSION = "0.2.6"
  const val OTEL_INSTRUMENTATION_VERSION = "2.28.0"
}

dependencies {
  // Open telemetry instrumentation
  implementation(
    platform(
      "io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:${Deps.OTEL_INSTRUMENTATION_VERSION}"
    )
  )
  implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")

  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
  implementation("tools.jackson.module:jackson-module-kotlin")
  implementation(
    "io.swagger.core.v3:swagger-annotations-jakarta:${Deps.SWAGGER_ANNOTATIONS_VERSION}"
  )
  implementation(
    "org.openapitools:jackson-databind-nullable:${Deps.JACKSON_DATABIND_NULLABLE_VERSION}"
  )

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

// Dependency locking - lock all dependencies
dependencyLocking { lockAllConfigurations() }

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

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>(
  "transactions-handler"
) {
  description = "Generate transactions-handler API client"
  group = "openapi-generation"
  generatorName.set("kotlin-spring")
  inputSpec.set("$rootDir/api-spec/transactions-handler-api.yaml")
  outputDir.set(layout.buildDirectory.get().dir("generated").asFile.toString())
  apiPackage.set("it.pagopa.generated.posgw.transactions.handler.api")
  modelPackage.set("it.pagopa.generated.posgw.transactions.handler.model")
  generateApiDocumentation.set(false)
  generateApiTests.set(false)
  generateModelTests.set(false)
  library.set("spring-boot")
  modelNameSuffix.set("Dto")
  configOptions.set(
    mapOf(
      "annotationLibrary" to "swagger2",
      "openApiNullable" to "true",
      "interfaceOnly" to "true",
      "hideGenerationTimestamp" to "true",
      "skipDefaultInterface" to "true",
      "useSwaggerUI" to "false",
      "reactive" to "true",
      "useSpringBoot3" to "true",
      "oas3" to "true",
      "generateSupportingFiles" to "true",
      "enumPropertyNaming" to "UPPERCASE",
    )
  )
}

sourceSets {
  named("main") {
    kotlin.srcDir(layout.buildDirectory.dir("generated/src/main/kotlin"))
  }
}

tasks.named("compileKotlin") {
  dependsOn("transactions-handler")
}

/**
 * Task used to expand application properties with build specific properties such as artifact name
 * and version
 */
tasks.processResources { filesMatching("application.properties") { expand(project.properties) } }

/** Semantic versioning plugin configuration */
tasks.named("incrementVersion") {
  dependsOn("prepareKotlinBuildScriptModel")
}
