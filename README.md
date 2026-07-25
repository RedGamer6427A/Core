Don't use this code... it's not very good...

## Compiling
For the testing, create a plugin-dirs.txt file in the testing module you want to compile.

## Usage
Shadow it into your plugin using these
(yes i do know github packages exist, deal with it)
settings.gradle.kts
```kotlin
includeBuild("/path/to/your/version/of/this/monster")
```
build.gradle.kts (paper/backend)
```kotlin
implementation("dev.redgamer6427a.core:core:2.0.0")
implementation("dev.redgamer6427a.core.minecraft.common:minecraft-common:2.0.0")
implementation("dev.redgamer6427a.core.minecraft.paper:minecraft-paper:2.0.0")
```

build.gradle.kts (velocity/proxy)
```kotlin
implementation("dev.redgamer6427a.core:core:2.0.0")
implementation("dev.redgamer6427a.core.minecraft.common:minecraft-common:2.0.0")
implementation("dev.redgamer6427a.core.minecraft.velocity:minecraft-velocity:2.0.0")
```

Java 25!
Have some contractually mandated fun! fun! fun!

## Known issues
- Two proxy plugins can't use the lib at once.
- It's bad code.
