import java.net.URI

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.1.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("maven-publish")

}

group = "dev.redgamer6427a.core.minecraft.paper"
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
    paperweight.paperDevBundle("26.1.2.build.+")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}