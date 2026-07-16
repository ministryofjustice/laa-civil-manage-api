plugins {
    java
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
    id("com.diffplug.spotless") version "7.2.1"
    id("com.adarshr.test-logger") version "4.0.0"
    id("com.github.ben-manes.versions") version "0.54.0"
}

group = "uk.gov.justice"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

// Temporary: override Spring Boot BOM's logback version to fix SNYK-JAVA-CHQOSLOGBACK-17675439
// (Expression Injection, High severity). Remove once Spring Boot ships with logback-core >= 1.5.36.
extra["logback.version"] = "1.5.36"

// Temporary: override Spring Boot BOM's Tomcat version to fix SNYK-JAVA-ORGAPACHETOMCATEMBED-17732890
// and SNYK-JAVA-ORGAPACHETOMCATEMBED-17733746. Remove once Spring Boot ships with tomcat-embed-core >= 11.0.23.
extra["tomcat.version"] = "11.0.23"

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

dependencyManagement {
    dependencies {
        dependency("com.fasterxml.jackson.core:jackson-databind:2.21.5")
        dependency("tools.jackson.core:jackson-databind:3.1.5")
    }
}

dependencies {
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

    testImplementation("io.rest-assured:spring-mock-mvc:6.0.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    implementation("org.springframework.boot:spring-boot-starter-web")
}

openApi {
    outputDir.set(layout.projectDirectory.dir("openApi"))
    groupedApiMappings.set(
        mapOf(
            "http://localhost:8080/v3/api-docs/laa-civil-manage-api" to "openApi.json",
            "http://localhost:8080/v3/api-docs/mock-access-data-store" to "mockAccessDataStore.json",
        ),
    )
    customBootRun {
        args.set(
            listOf(
                "--spring.security.oauth2.client.registration.ads-api.client-id=openapi-doc-gen",
                "--spring.security.oauth2.client.registration.ads-api.client-secret=openapi-doc-gen",
                "--AZURE_ENTRA_TENANT_ID=openapi-doc-gen-tenant",
                "--ADS_TENANT_DOMAIN=devlexternal.onmicrosoft.com",
                "--ADS_CLIENT_ID=openapi-doc-gen-ads",
            ),
        )
    }
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

testlogger {
    theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
}
