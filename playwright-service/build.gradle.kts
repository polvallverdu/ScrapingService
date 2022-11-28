plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    implementation("com.microsoft.playwright:playwright:1.28.0")

    implementation(project(":common"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}