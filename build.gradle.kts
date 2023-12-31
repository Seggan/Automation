plugins {
    kotlin("jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.3"
    id("xyz.jpenilla.run-paper") version "2.2.0"
}

group = "io.github.seggan"
version = "UNOFFICIAL"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    library(kotlin("stdlib"))

    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("com.github.Slimefun:Slimefun4:RC-36")
    implementation("org.bstats:bstats-bukkit:3.0.2")

    api("com.github.Seggan:metis:c0bde92f08")

    library("org.java-websocket:Java-WebSocket:1.5.4")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

bukkit {
    name = "Automation"
    main = "io.github.seggan.automation.Automation"
    version = project.version.toString()
    author = "Seggan"
    apiVersion = "1.20"
    depend = listOf("Slimefun")
    commands {
        register("automation") {
            description = "Automation plugin command"
            aliases = listOf("auto")
        }
    }
}

tasks.shadowJar {
    dependsOn(tasks.test)
    relocate("io.github.seggan.metis", "io.github.seggan.automation.metis") {
        exclude("META-INF/**")
    }
    relocate("org.bstats", "io.github.seggan.automation.bstats") {
        exclude("META-INF/**")
    }
}

tasks.runServer {
    downloadPlugins {
        url("https://blob.build/dl/Slimefun4/Dev/1116")
        url("https://thebusybiscuit.github.io/builds/SchnTgaiSpock/SlimeHUD/master/SlimeHUD-11.jar")
    }
    maxHeapSize = "2G"
    minecraftVersion("1.20.1")
}