// Root build.gradle.kts - place in repo root

// Make lombokVersion available via gradle.properties (see step 1)
val lombokVersion = "1.18.42" // or by project; reading from settings requires it to be in gradle.properties accessible here
// If the above line fails in your Gradle version, use: val lombokVersion: String by rootProject

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    // Only add Lombok to projects that apply the java plugin
    pluginManager.withPlugin("java") {
        dependencies {
            add("compileOnly", "org.projectlombok:lombok:$lombokVersion")
            add("annotationProcessor", "org.projectlombok:lombok:$lombokVersion")

        }
        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(25))
        }
    }
}

allprojects {
    version = project.property("projectVersion") as String
}