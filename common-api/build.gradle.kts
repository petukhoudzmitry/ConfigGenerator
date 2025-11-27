plugins {
    kotlin("jvm")
    alias(libs.plugins.java.gradle.plugin)
    `java-library`
}

group = "org.delyo"
version = "1.0.0"

java {
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation(libs.bundles.jackson)
    implementation(libs.kotlinpoet)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}