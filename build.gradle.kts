plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gatling)
}

group = "ru.itis.auto"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.gatling.core.java)
    implementation(libs.gatling.http.java)
    implementation(libs.gatling.charts)

    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.datatype.jsr310)

    gatling(libs.jackson.dataformat.yaml)
    gatling(libs.jackson.module.kotlin)
    gatling(libs.jackson.datatype.jsr310)

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