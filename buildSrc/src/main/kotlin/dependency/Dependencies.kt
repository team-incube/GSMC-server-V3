package dependency

object Dependencies {
    /* Spring Starters */
    const val SPRING_WEB = "org.springframework.boot:spring-boot-starter-web"
    const val SPRING_VALIDATION = "org.springframework.boot:spring-boot-starter-validation"
    const val SPRING_SECURITY = "org.springframework.boot:spring-boot-starter-security"
    const val SPRING_ACTUATOR = "org.springframework.boot:spring-boot-starter-actuator"
    const val SPRING_OAUTH2_CLIENT = "org.springframework.boot:spring-boot-starter-oauth2-client"

    /* Spring Data */
    const val SPRING_DATA_JPA = "org.springframework.boot:spring-boot-starter-data-jpa"
    const val SPRING_DATA_REDIS = "org.springframework.boot:spring-boot-starter-data-redis"

    /* Exposed ORM */
    const val EXPOSED_CORE = "org.jetbrains.exposed:exposed-core:${DependencyVersions.EXPOSED_VERSION}"
    const val EXPOSED_DAO = "org.jetbrains.exposed:exposed-dao:${DependencyVersions.EXPOSED_VERSION}"
    const val EXPOSED_JDBC = "org.jetbrains.exposed:exposed-jdbc:${DependencyVersions.EXPOSED_VERSION}"
    const val EXPOSED_JAVA_TIME = "org.jetbrains.exposed:exposed-java-time:${DependencyVersions.EXPOSED_VERSION}"
    const val EXPOSED_SPRING_BOOT = "org.jetbrains.exposed:exposed-spring-boot-starter:${DependencyVersions.EXPOSED_VERSION}"

    /* HikariCP */
    const val HIKARI_CP = "com.zaxxer:HikariCP"

    /* Development Tools */
    const val SPRING_DOCKER_SUPPORT = "org.springframework.boot:spring-boot-docker-compose"
    const val SPRING_DEVTOOLS = "org.springframework.boot:spring-boot-devtools"

    /* Kotlin */
    const val JACKSON_KOTLIN = "com.fasterxml.jackson.module:jackson-module-kotlin"
    const val KOTLIN_REFLECT = "org.jetbrains.kotlin:kotlin-reflect"

    /* Spring Cloud */
    const val SPRING_CLOUD_FEIGN = "org.springframework.cloud:spring-cloud-starter-openfeign:${DependencyVersions.SPRING_CLOUD_FEIGN_VERSION}"
    const val FEIGN_JACKSON = "io.github.openfeign:feign-jackson:${DependencyVersions.FEIGN_JACKSON_VERSION}"

    /* Database */
    const val MYSQL_CONNECTOR = "com.mysql:mysql-connector-j"

    /* Swagger */
    const val SWAGGER_UI = "org.springdoc:springdoc-openapi-starter-webmvc-ui:${DependencyVersions.SWAGGER_VERSION}"

    /* AWS */
    const val AWS_S3_SDK = "io.awspring.cloud:spring-cloud-aws-starter-s3:${DependencyVersions.AWS_S3_SDK_VERSION}"
    const val AWS_LOGBACK_APPENDER = "ca.pjer:logback-awslogs-appender:${DependencyVersions.AWS_LOGBACK_APPENDER_VERSION}"

    /* Environment Variables */
    const val DOTENV = "me.paulschwarz:spring-dotenv:${DependencyVersions.DOTENV_VERSION}"

    /* Testing */
    const val SPRING_TEST = "org.springframework.boot:spring-boot-starter-test"
    const val KOTLIN_JUNIT5 = "org.jetbrains.kotlin:kotlin-test-junit5"
    const val KOTEST = "io.kotest:kotest-assertions-core:${DependencyVersions.KOTEST_VERSION}"
    const val KOTEST_RUNNER = "io.kotest:kotest-runner-junit5:${DependencyVersions.KOTEST_VERSION}"
    const val SPRING_SECURITY_TEST = "org.springframework.security:spring-security-test"
    const val JUNIT_PLATFORM_LAUNCHER = "org.junit.platform:junit-platform-launcher"
    const val MOCKK = "io.mockk:mockk:${DependencyVersions.MOCKK_VERSION}"

    // JWT
    const val JJWT_API = "io.jsonwebtoken:jjwt-api:${DependencyVersions.JJWT_VERSION}"
    const val JJWT_IMPL = "io.jsonwebtoken:jjwt-impl:${DependencyVersions.JJWT_VERSION}"
    const val JJWT_JACKSON = "io.jsonwebtoken:jjwt-jackson:${DependencyVersions.JJWT_VERSION}"

    // Apache POI (Excel)
    const val APACHE_POI = "org.apache.poi:poi:${DependencyVersions.APACHE_POI_VERSION}"
    const val APACHE_POI_OOXML = "org.apache.poi:poi-ooxml:${DependencyVersions.APACHE_POI_VERSION}"
}
