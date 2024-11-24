plugins {
    java
    id("org.springframework.boot") version "3.3.6"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.asciidoctor.jvm.convert") version "3.3.2" // Asciidoctor 플러그인 추가
}

group = "com.chaw"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // BOM 을 사용하여 의존성 버전을 관리
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")  // Spring MVC 추가
    implementation("org.projectlombok:lombok")  // Lombok 추가
    implementation("com.mysql:mysql-connector-j") // MySQL 의존성 추가
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // Spring Data JPA 추가
    implementation("org.springframework.boot:spring-boot-starter-security") // Spring Security 의존성 추가
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign") // OpenFeign 의존성 추가
    implementation("org.springframework.kafka:spring-kafka") // Kafka 의존성 추가

    // BOM 에 포함되지 않아 직접 버전을 명시
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0") // Swagger 의존성 추가
    implementation("io.jsonwebtoken:jjwt-api:0.11.5") // JWT API 의존성 추가
    implementation("jakarta.servlet:jakarta.servlet-api:6.0.0") // Jakarta Servlet API 의존성 추가
    implementation("org.redisson:redisson-spring-boot-starter:3.23.4") // Redisson 의존성 추가 (버전 3.23.4)

    annotationProcessor("org.projectlombok:lombok")  // Lombok 컴파일 타임에 사용

    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5") // JWT 구현체
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5") // JSON 처리

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.rest-assured:rest-assured:5.3.0") // RestAssured 의존성 추가
    testImplementation("io.rest-assured:json-path:5.3.0") // JSONPath 의존성 추가
    testImplementation("org.springframework.kafka:spring-kafka-test") // Kafka 테스트 의존성 추가

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.3")
    }
}

tasks.withType<Test> {
	useJUnitPlatform()
    jvmArgs = listOf("-Xshare:off")
}
