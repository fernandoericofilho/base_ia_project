import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
    kotlin("plugin.jpa") version "1.9.24"
    jacoco
}

group = "com.base"
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
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.flywaydb:flyway-core")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("com.h2database:h2")
    // Flyway 10+: suporte ao PostgreSQL vive neste módulo (sem ele: "Unsupported Database: PostgreSQL 16.x").
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
    // Regras de arquitetura (camadas, injeção por construtor, LAZY, sem Double em dinheiro) — REGRAS §2.3
    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")

    // Testes de integracao (Testcontainers) - app continua em H2 no dia a dia;
    // Postgres real e usado apenas nesta camada de teste, via Docker.
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
    testImplementation("org.testcontainers:postgresql:1.20.4")
    testRuntimeOnly("org.postgresql:postgresql")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.test {
    useJUnitPlatform {
        excludeTags("integration")
    }
}

tasks.register<Test>("integrationTest") {
    description = "Executa testes de integracao com Postgres real via Testcontainers (requer Docker)"
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform {
        includeTags("integration")
    }
    shouldRunAfter(tasks.test)
}


// Cobertura de linha (REGRAS §11, 2026-10-22). Conta só o `test` (unitário); integração não entra.
jacoco {
    toolVersion = "0.8.12"
}

// Fora do cálculo: código sem regra própria (REGRAS §11). Controllers são pass-through,
// cobertos pelo integrationTest, então não contam no piso do `test`.
val coverageExcludes = listOf(
    "com/base/ModeloApplication*",
    "com/base/models/**",
    "com/base/dtos/**",
    "com/base/controllers/**",
    "com/base/exceptions/**",
    "com/base/config/**",
)

val coverageClassDirectories = provider {
    sourceSets.main.get().output.classesDirs.asFileTree.matching { exclude(coverageExcludes) }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    classDirectories.setFrom(coverageClassDirectories)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    classDirectories.setFrom(coverageClassDirectories)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = providers.gradleProperty("coverageFloor").get().toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
