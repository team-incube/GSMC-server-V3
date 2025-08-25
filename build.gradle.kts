plugins {
    id(plugin.Plugins.SPRING_BOOT) version plugin.PluginVersions.SPRING_BOOT_VERSION;
    id(plugin.Plugins.DEPENDENCY_MANAGEMENT) version plugin.PluginVersions.DEPENDENCY_MANAGEMENT_VERSION;
    id(plugin.Plugins.KAPT) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTLIN_JVM) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTLIN_SPRING) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTLIN_JPA) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTLIN_ALLOPEN) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTEST) version plugin.PluginVersions.KOTEST_VERSION
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
    implementation(dependency.Dependencies.SPRING_DATA_JPA)
    implementation(dependency.Dependencies.SPRING_DATA_REDIS)
    implementation(dependency.Dependencies.SPRING_SECURITY)
    implementation(dependency.Dependencies.SPRING_VALIDATION)
    implementation(dependency.Dependencies.SPRING_WEB)

    implementation(dependency.Dependencies.JACKSON_KOTLIN)
    implementation(dependency.Dependencies.KOTLIN_REFLECT)
    implementation(dependency.Dependencies.SPRING_CLOUD_FEIGN)
    runtimeOnly(dependency.Dependencies.MYSQL_CONNECTOR)

    testImplementation(dependency.Dependencies.SPRING_TEST)
    testImplementation(dependency.Dependencies.KOTLIN_JUNIT5)
    testImplementation(dependency.Dependencies.KOTEST)
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
