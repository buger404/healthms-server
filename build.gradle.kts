val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "1.9.24"
    id("io.ktor.plugin") version "2.3.10"
}

group = "para.party"
version = "0.0.1"

application {
    mainClass.set("para.party.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    api("io.ktor:ktor-server-auto-head-response-jvm")
    api("io.ktor:ktor-server-core-jvm")
    api("io.ktor:ktor-server-host-common-jvm")
    api("io.ktor:ktor-server-netty-jvm")
    api("io.ktor:ktor-server-content-negotiation")
    api("io.ktor:ktor-server-status-pages-jvm")

    api("io.ktor:ktor-serialization-jackson:$ktor_version")

    api("ch.qos.logback:logback-classic:$logback_version")

    testApi("io.ktor:ktor-server-tests-jvm")
    testApi("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")


    // https://mvnrepository.com/artifact/com.zaxxer/HikariCP
    api("com.zaxxer:HikariCP:5.1.0")
    // https://mvnrepository.com/artifact/org.ktorm/ktorm-core
    api("org.ktorm:ktorm-core:3.6.0")
    // https://mvnrepository.com/artifact/org.ktorm/ktorm-jackson
    api("org.ktorm:ktorm-jackson:3.6.0")

    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.1")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1");
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    compileOnly("com.fasterxml.jackson.module:jackson-module-parameter-names:2.17.1")
    compileOnly("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.17.1")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.1")

    api(files("libraries/com.sinodbms.jdbc.jar"))
}
