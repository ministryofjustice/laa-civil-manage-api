plugins {
    java
    id("org.springframework.boot") version "4.0.7"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
    id("com.diffplug.spotless") version "7.2.1"
}

group = "uk.gov.justice"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("io.rest-assured:spring-mock-mvc:6.0.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.fasterxml.jackson.core:jackson-databind")
}

openApi {
    outputDir.set(layout.projectDirectory.dir("openApi"))
    groupedApiMappings.set(
        mapOf(
            "http://localhost:8080/v3/api-docs/laa-civil-manage-api" to "openApi.json",
            "http://localhost:8080/v3/api-docs/mock-access-data-store" to "mockAccessDataStore.json",
        ),
    )
}

spotless {
    java {
        target("src/*/java/**/*.java")
        googleJavaFormat("1.30.0")
    }

    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }

    format("misc") {
        target("*.md", "*.yml", "*.yaml")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.register("verifyOpenApiSync") {
    dependsOn("generateOpenApiDocs")

    group = "verification"

    doLast {
        val openApiDir = file("openApi")

        val status =
            providers
                .exec {
                    commandLine("git", "status", "--porcelain", openApiDir.absolutePath)
                }.standardOutput.asText
                .get()
                .trim()

        if (status.isNotEmpty()) {
            throw GradleException(
                "ERROR: OpenAPI schemas are out of sync with your code changes!\n" +
                    "Files under 'openApi/' have changed. Please commit the updated versions (regenerate locally using ./gradlew generateOpenApiDocs).",
            )
        }

        println("OpenAPI sync verified: No changes detected.")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
