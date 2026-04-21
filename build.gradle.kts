plugins {
	java
	id("org.springframework.boot") version "4.0.5"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
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

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

	implementation ("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
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
	
    // This addresses a vulnerability with transient dependency in Jackson serializer. 
	constraints {
        implementation("com.fasterxml.jackson.core:jackson-core") {
            version {
                strictly("2.21.1")
            }
            because("version 2.20.2 has a DoS vulnerability (CVE-2025-52999)")
        }
    }	
}

openApi {
    apiDocsUrl.set("http://localhost:8080/v3/api-docs")
    outputDir.set(layout.projectDirectory.dir("openApi"))
    outputFileName.set("openApi.json") 
}

tasks.register("verifyOpenApiSync") {
    dependsOn("generateOpenApiDocs")
    
    group = "verification"
    
    doLast {
        val generatedFile = file("openApi/openApi.json")

        val status = providers.exec {
            commandLine("git", "status", "--porcelain", generatedFile.absolutePath)
        }.standardOutput.asText.get().trim()

        if (status.isNotEmpty()) {
            throw GradleException(
                "ERROR: OpenAPI schema is out of sync with your code changes!\n" +
                "The file 'openApi/openApi.json' has changed. Please commit the updated version (this can be generated locally using ./gradlew generateOpenApiDocs)."
            )
        }
        
        println("OpenAPI sync verified: No changes detected.")
    }
}

tasks.withType<Test> {
	useJUnitPlatform()
}
