dependencies {
    implementation("org.springframework.retry:spring-retry:2.0.13")
    implementation("org.springframework:spring-aop")
    implementation("org.springframework:spring-context")
    implementation("org.aspectj:aspectjweaver")
    implementation("uk.gov.service.notify:notifications-java-client:5.2.1-RELEASE")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.18.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
