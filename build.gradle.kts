plugins {
    alias(libs.plugins.kotlin.jvm)

    id("io.gatling.gradle") version "3.13.1"
}

group = "gg.smurf.vdgd"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.gatling:gatling-core-java:3.13.1")
    implementation("io.gatling:gatling-http-java:3.13.1")
    implementation("io.gatling.highcharts:gatling-charts-highcharts:3.13.1")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    gatling("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    gatling("com.fasterxml.jackson.module:jackson-module-kotlin")
    gatling("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    testImplementation(libs.kotlin.test)
    testImplementation(libs.selenide)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.slf4j)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}