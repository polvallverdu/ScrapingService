plugins {
    id("java")
}

group = "me.java"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    implementation(project(":common"))
    implementation("com.github.code4craft:xsoup:xsoup-0.3.6")  // Already adds jsoup
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}