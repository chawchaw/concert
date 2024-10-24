import org.asciidoctor.gradle.jvm.AsciidoctorTask

plugins {
    java
    id("org.springframework.boot") version "3.3.4"
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
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")  // Spring MVC 추가
    implementation("org.projectlombok:lombok:1.18.30")  // Lombok 추가
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0") // Swagger 의존성 추가
    implementation("mysql:mysql-connector-java:8.0.33") // MySQL 8.0 의존성 추가
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // Spring Data JPA 추가

    annotationProcessor("org.projectlombok:lombok")  // Lombok 컴파일 타임에 사용

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
