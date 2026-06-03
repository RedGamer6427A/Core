plugins {
    id("java")
}

group = "dev.redgamer6427a.core.mc"
version = "2.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
}
