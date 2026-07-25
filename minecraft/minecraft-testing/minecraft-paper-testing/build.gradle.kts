import java.net.URI

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.1.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
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

    paperweight.paperDevBundle("26.1.2.build.+")

}

tasks.processResources {

    val props = mapOf("version" to version.toString())
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
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
