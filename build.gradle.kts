plugins {
    kotlin("jvm") version "1.9.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.3"
    id("xyz.jpenilla.run-paper") version "2.2.0"
}

group = "io.github.seggan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    library(kotlin("stdlib"))
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("com.github.Slimefun:Slimefun4:RC-35")

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
}

tasks.runServer {
    downloadPlugins {
        url("https://thebusybiscuit.github.io/builds/TheBusyBiscuit/Slimefun4/master/Slimefun4-1104.jar")
    }
    minecraftVersion("1.20.1")
}