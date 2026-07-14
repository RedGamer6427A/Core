plugins {
    id("java")
}

group = "dev.redgamer6427a.core"
version = "2.0.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")

    implementation("com.google.guava:guava:32.1.2-jre")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.xerial:sqlite-jdbc:3.49.1.0")
    implementation("org.jline:jline:3.29.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.bouncycastle:bcpkix-jdk18on:1.78")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78")
}


tasks.register("prepareKotlinBuildScriptModel"){}
tasks.register("paperweightUserdevSetup")