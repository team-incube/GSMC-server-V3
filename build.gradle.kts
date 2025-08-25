plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "1.9.25"
}

group = "com.team.incube"
version = "0.0.1-SNAPSHOT"
description = "GSM 인증제 관리 서비스 GSMC v3 서버 애플리케이션"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2025.0.0"

dependencies {
    implementation(Dependencies.SPRING_DATA_JPA)
    implementation(Dependencies.SPRING_DATA_REDIS)
    implementation(Dependencies.SPRING_SECURITY)
    implementation(Dependencies.SPRING_VALIDATION)
    implementation(Dependencies.SPRING_WEB)

    implementation(Dependencies.JACKSON_KOTLIN)
    implementation(Dependencies.KOTLIN_REFLECT)
    implementation(Dependencies.SPRING_CLOUD_FEIGN)
    runtimeOnly(Dependencies.MYSQL_CONNECTOR)

    testImplementation(Dependencies.SPRING_TEST)
    testImplementation(Dependencies.KOTLIN_JUNIT5)
    testImplementation(Dependencies.KOTEST)
    testImplementation(Dependencies.SPRING_SECURITY_TEST)
    testRuntimeOnly(Dependencies.JUNIT_PLATFORM_LAUNCHER)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
