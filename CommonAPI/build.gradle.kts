plugins {
    kotlin("jvm")
    alias(libs.plugins.java.gradle.plugin)
}

group = "org.delyo.configgen.api"
version = "1.0.0"

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