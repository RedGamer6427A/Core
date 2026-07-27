import java.net.URI

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.1.0"
}

group = "dev.redgamer6427a.core.minecraft.velocity.testing"


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
    implementation(project(":minecraft:minecraft-velocity"))

    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
}

val pluginDirsFile = file("plugin-dirs.txt") // newline-separated folder list
val pluginJarName: String by lazy {
    (findProperty("pluginJarName") as String?) ?: project.name
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveBaseName.set(pluginJarName)
    dependsOn("processResources")
    doLast {
        val jar = archiveFile.get().asFile
        pluginDirsFile.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { dir ->
                copy {
                    from(jar)
                    into(file(dir))
                }
            }
    }
}
