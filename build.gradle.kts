plugins {
    id(plugin.Plugins.SPRING_BOOT) version plugin.PluginVersions.SPRING_BOOT_VERSION
    id(plugin.Plugins.DEPENDENCY_MANAGEMENT) version plugin.PluginVersions.DEPENDENCY_MANAGEMENT_VERSION
    id(plugin.Plugins.KSP) version plugin.PluginVersions.KSP_VERSION
    id(plugin.Plugins.KOTLIN_JVM) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTLIN_SPRING) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTLIN_JPA) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTLIN_ALLOPEN) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTEST) version plugin.PluginVersions.KOTEST_VERSION
    id(plugin.Plugins.KTLINT) version plugin.PluginVersions.KTLINT_VERSION
    idea
}

group = "com.team.incube"
version = "0.0.1-SNAPSHOT"
description = "GSM 인증제 관리 서비스 GSMC v3 서버 애플리케이션"
java.sourceCompatibility = JavaVersion.VERSION_24

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

extra["springCloudVersion"] = plugin.PluginVersions.SPRING_CLOUD_VERSION

dependencies {
    // Spring Starters
    implementation(dependency.Dependencies.SPRING_WEB)
    implementation(dependency.Dependencies.SPRING_VALIDATION)
    implementation(dependency.Dependencies.SPRING_SECURITY)
    implementation(dependency.Dependencies.SPRING_ACTUATOR)
    implementation(dependency.Dependencies.SPRING_OAUTH2_CLIENT)

    // Spring Data
    implementation(dependency.Dependencies.SPRING_DATA_REDIS)

    // Exposed ORM
    implementation(dependency.Dependencies.EXPOSED_CORE)
    implementation(dependency.Dependencies.EXPOSED_DAO)
    implementation(dependency.Dependencies.EXPOSED_JDBC)
    implementation(dependency.Dependencies.EXPOSED_JAVA_TIME)
    implementation(dependency.Dependencies.EXPOSED_SPRING_BOOT)

    // HikariCP
    implementation(dependency.Dependencies.HIKARI_CP)

    // Development Tools
    developmentOnly(dependency.Dependencies.SPRING_DOCKER_SUPPORT)
    developmentOnly(dependency.Dependencies.SPRING_DEVTOOLS)

    // Kotlin
    implementation(dependency.Dependencies.JACKSON_KOTLIN)
    implementation(dependency.Dependencies.KOTLIN_REFLECT)

    // Spring Cloud
    implementation(dependency.Dependencies.SPRING_CLOUD_FEIGN)
    implementation(dependency.Dependencies.FEIGN_JACKSON)

    // Database
    runtimeOnly(dependency.Dependencies.MYSQL_CONNECTOR)

    // Swagger
    implementation(dependency.Dependencies.SWAGGER_UI)

    // AWS
    implementation(dependency.Dependencies.AWS_S3_SDK)
    implementation(dependency.Dependencies.AWS_LOGBACK_APPENDER)

    // Environment Variables
    implementation(dependency.Dependencies.DOTENV)

    // Testing
    testImplementation(dependency.Dependencies.SPRING_TEST)
    testImplementation(dependency.Dependencies.KOTLIN_JUNIT5)
    testImplementation(dependency.Dependencies.KOTEST)
    testImplementation(dependency.Dependencies.KOTEST_RUNNER)
    testImplementation(dependency.Dependencies.KOTEST_EXTENSIONS_SPRING)
    testImplementation(dependency.Dependencies.SPRING_SECURITY_TEST)
    testRuntimeOnly(dependency.Dependencies.JUNIT_PLATFORM_LAUNCHER)
    testImplementation(dependency.Dependencies.MOCKK)
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

tasks.withType<Test> {
    useJUnitPlatform()
}

idea {
    module {
        val kspMain = file("build/generated/ksp/main/kotlin")
        sourceDirs.add(kspMain)
        generatedSourceDirs.add(kspMain)
    }
}
