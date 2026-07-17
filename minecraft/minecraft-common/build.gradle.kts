plugins {
    id("java")
}

group = "dev.redgamer6427a.core.minecraft.common"
version = "2.0.0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://libraries.minecraft.net")
    }
}

dependencies {
    implementation(project(":core"))
    implementation("com.mojang:brigadier:1.0.18")
    implementation("net.kyori:adventure-text-minimessage:5.2.0")
    implementation("net.kyori:adventure-api:5.2.0")
    implementation("com.google.guava:guava:32.1.2-jre")
    implementation("com.google.code.gson:gson:2.11.0")

}
