plugins {
    java
}

group = "ru.javaroot.pafk"
version = providers.gradleProperty("pluginVersion").get()
description = "AFK zones plugin for Paper and Folia 1.21-26.4"

val targetJava = 21

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(targetJava))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.12") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.9") {
        exclude(group = "org.bukkit", module = "bukkit")
    }

    testImplementation("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

configurations.configureEach {
    resolutionStrategy {
        force(
            "com.google.guava:guava:33.3.1-jre",
            "com.google.code.gson:gson:2.11.0",
            "it.unimi.dsi:fastutil:8.5.15"
        )
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(targetJava)
    }

    compileTestJava {
        options.encoding = "UTF-8"
        options.release.set(targetJava)
    }

    test {
        useJUnitPlatform()
    }

    processResources {
        filteringCharset = "UTF-8"
        val props = mapOf("version" to project.version.toString())
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
