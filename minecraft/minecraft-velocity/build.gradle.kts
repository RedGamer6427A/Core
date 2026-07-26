import java.net.URI

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.1.0"
    id("maven-publish")

}

group = "dev.redgamer6427a.core.minecraft.velocity"
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
    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
    implementation("dev.dejvokep:boosted-yaml:1.3.6")
    annotationProcessor("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}