pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

include("minecraft:minecraft-common")
include("minecraft:minecraft-paper")
include("core")
include("minecraft:minecraft-testing")
include("minecraft:minecraft-testing:minecraft-paper-testing")
include("minecraft:minecraft-velocity")
include("minecraft:minecraft-testing:minecraft-velocity-testing")