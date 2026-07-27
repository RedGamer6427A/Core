plugins {
    id("java")
    id("maven-publish")

}

group = "dev.redgamer6427a.core.minecraft.common"


repositories {
    mavenCentral()

}

dependencies {
    implementation(project(":core"))
    implementation("net.kyori:adventure-text-minimessage:5.2.0")
    implementation("net.kyori:adventure-api:5.2.0")
    implementation("com.google.guava:guava:32.1.2-jre")
    implementation("com.google.code.gson:gson:2.11.0")

}
