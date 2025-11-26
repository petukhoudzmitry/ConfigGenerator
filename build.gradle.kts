plugins {
    kotlin("jvm") version "2.2.20"
    alias(libs.plugins.java.gradle.plugin)
    `maven-publish`
}

tasks.checkKotlinGradlePluginConfigurationErrors {
    enabled = true
}

group = "org.delyo"
version = "1.0.0"

repositories {
    mavenCentral()
}

val generateLocalProperties: PluginDeclaration by gradlePlugin.plugins.creating {
    id = "org.delyo.gradle.configgen"
    implementationClass = "org.delyo.gradle.configgen.plugin.ConfigGeneratorPlugin"
}

dependencies {
    testImplementation(kotlin("test"))

    implementation(project(":CommonAPI"))
    implementation(libs.kotlinpoet)
    implementation(libs.bundles.jackson)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}