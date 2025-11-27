plugins {
    kotlin("jvm")
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
    implementation(project(":common-api"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}