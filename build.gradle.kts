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

    annotationProcessor("org.projectlombok:lombok")  // Lombok 컴파일 타임에 사용

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc") // Spring REST Docs MockMVC 추가
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

val snippetsDir = file("build/generated-snippets")

tasks.named<Test>("test") {
    outputs.dir(snippetsDir)
    doFirst {
        delete(snippetsDir)
    }
}

tasks.named<AsciidoctorTask>("asciidoctor") {
    inputs.dir(snippetsDir)
    dependsOn(tasks.test)
    attributes(
        mapOf("snippets" to snippetsDir.absolutePath)
    )
    outputOptions {
        setOutputDir(file("build/docs/asciidoc"))
    }
}

tasks.register<Copy>("copyRestDocs") {
    from("build/docs/asciidoc")
    into("src/main/resources/static/docs")
    dependsOn(tasks.withType<AsciidoctorTask>())
}
