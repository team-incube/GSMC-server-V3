package dependency

object Dependencies {
    /* Spring Starters */
    const val SPRING_WEB = "org.springframework.boot:spring-boot-starter-web"
    const val SPRING_VALIDATION = "org.springframework.boot:spring-boot-starter-validation"
    const val SPRING_SECURITY = "org.springframework.boot:spring-boot-starter-security"

    /* Spring Data */
    const val SPRING_DATA_JPA = "org.springframework.boot:spring-boot-starter-data-jpa"
    const val SPRING_DATA_REDIS = "org.springframework.boot:spring-boot-starter-data-redis"

    /* Development Tools */
    const val SPRING_DOCKER_SUPPORT = "org.springframework.boot:spring-boot-docker-compose"
    const val SPRING_DEVTOOLS = "org.springframework.boot:spring-boot-devtools"

    /* Kotlin */
    const val JACKSON_KOTLIN = "com.fasterxml.jackson.module:jackson-module-kotlin"
    const val KOTLIN_REFLECT = "org.jetbrains.kotlin:kotlin-reflect"

    /* Spring Cloud */
    const val SPRING_CLOUD_FEIGN = "org.springframework.cloud:spring-cloud-starter-openfeign:${DependencyVersions.SPRING_CLOUD_FEIGN_VERSION}"

    /* Database */
    const val MYSQL_CONNECTOR = "com.mysql:mysql-connector-j"

    /* Swagger */
    const val SWAGGER_UI = "org.springdoc:springdoc-openapi-starter-webmvc-ui:${DependencyVersions.SWAGGER_VERSION}"

    /* Testing */
    const val SPRING_TEST = "org.springframework.boot:spring-boot-starter-test"
    const val KOTLIN_JUNIT5 = "org.jetbrains.kotlin:kotlin-test-junit5"
    const val KOTEST = "io.kotest:kotest-assertions-core:${DependencyVersions.KOTEST_VERSION}"
    const val SPRING_SECURITY_TEST = "org.springframework.security:spring-security-test"
    const val JUNIT_PLATFORM_LAUNCHER = "org.junit.platform:junit-platform-launcher"
    const val MOCKK = "io.mockk:mockk:${DependencyVersions.MOCKK_VERSION}"
}