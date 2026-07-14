import java.net.URI

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.1.0"
}

group = "dev.redgamer6427a.core.minecraft.paper.testing"
version = "2.0.0"

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = URI("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {

    implementation(project(":core"))
    implementation(project(":minecraft:minecraft-common"))
    implementation(project(":minecraft:minecraft-paper"))

    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
}

tasks.processResources {

    val props = mapOf("version" to version.toString())
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

val pluginsDir = "/home/red/Servers/Minecraft/Core Testing/Proxy/plugins"

tasks.register<Copy>("copyJar") {
    dependsOn(tasks.shadowJar)

    from(tasks.shadowJar.flatMap { it.archiveFile })
    into(layout.projectDirectory.dir("out/jars"))
}

tasks.shadowJar {
    archiveClassifier.set("")
    dependsOn ("processResources")
    doLast {
        copy {
            from(archiveFile)
            into(file(pluginsDir))
        }
    }
}

