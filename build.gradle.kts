plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "gg.smurf.vdgd"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
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