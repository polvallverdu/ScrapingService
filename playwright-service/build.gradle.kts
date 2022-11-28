plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    implementation(project(":common"))
    implementation("com.microsoft.playwright:playwright:1.28.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}