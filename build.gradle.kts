plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.jpa") version "1.9.23"
    kotlin("plugin.allopen") version "1.9.23"
    kotlin("plugin.spring") version "1.9.0"
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    id("jacoco")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

group = "br.com.itau"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.flywaydb:flyway-core")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Spring Kafka
    implementation("org.springframework.kafka:spring-kafka")

    // Testcontainers Kafka (p/ testes de integração, se quiser usar depois)
    testImplementation("org.testcontainers:kafka:1.20.1")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.35.0")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("com.ninja-squad:springmockk:4.0.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jacoco {
    toolVersion = "0.8.10"
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/domain/enums/**",
                    "**/domain/valueobjects/**",
                    "**/api/dto/**",
                    "**/adapters/config/**",
                    "**/interfaces/**",
                    "**/*Enum.class",
                    "**/*Dto.class",
                    "**/*Mapper.class"
                )
            }
        })
    )
}
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}


sourceSets {
    main {
        kotlin {
            srcDirs("src/main/kotlin")
        }
    }
    test {
        kotlin {
            srcDirs("src/test/kotlin")  // Aponta para test/ em vez de src/test/
        }
    }
}