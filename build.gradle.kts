plugins {
    id(plugin.Plugins.SPRING_BOOT) version plugin.PluginVersions.SPRING_BOOT_VERSION
    id(plugin.Plugins.DEPENDENCY_MANAGEMENT) version plugin.PluginVersions.DEPENDENCY_MANAGEMENT_VERSION
    id(plugin.Plugins.KAPT) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTLIN_JVM) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTLIN_SPRING) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTLIN_JPA) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTEST) version plugin.PluginVersions.KOTEST_VERSION
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
    /* Spring Starters */
    implementation(dependency.Dependencies.SPRING_WEB)
    implementation(dependency.Dependencies.SPRING_VALIDATION)
    implementation(dependency.Dependencies.SPRING_SECURITY)

    /* Spring Data */
    implementation(dependency.Dependencies.SPRING_DATA_JPA)
    implementation(dependency.Dependencies.SPRING_DATA_REDIS)

    /* Development Tools */
    developmentOnly(dependency.Dependencies.SPRING_DOCKER_SUPPORT)
    developmentOnly(dependency.Dependencies.SPRING_DEVTOOLS)

    /* Kotlin */
    implementation(dependency.Dependencies.JACKSON_KOTLIN)
    implementation(dependency.Dependencies.KOTLIN_REFLECT)

    /* Spring Cloud */
    implementation(dependency.Dependencies.SPRING_CLOUD_FEIGN)

    /* Database */
    runtimeOnly(dependency.Dependencies.MYSQL_CONNECTOR)

    /* Testing */
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

idea {
    module {
        val kaptMain = file("build/generated/source/kapt/main")
        sourceDirs.add(kaptMain)
        generatedSourceDirs.add(kaptMain)
    }
}