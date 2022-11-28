plugins {
    id("java")
}


subprojects {
    apply {
        plugin("java")
    }
}

allprojects {
    group = "me.java"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")


        implementation("com.rabbitmq:amqp-client:5.16.0")
        implementation("redis.clients:jedis:4.3.0")
        implementation("io.github.cdimascio:dotenv-java:2.3.1")
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}