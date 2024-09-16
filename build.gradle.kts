plugins {
    java
    `kotlin-dsl`
    `maven-publish`
    id("com.gradleup.shadow") version("8.3.0")
}

group = "org.lushplugins"
version = "2.0.0-alpha1"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.2")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9")
    shadow(files("libs/EnchantedStorage.jar"))
    shadow("com.github.CoolDCB:ChatColorHandler:v2.1.0")
}

java {
    configurations.shadow.get().dependencies.remove(dependencies.gradleApi())
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.shadowJar {
    minimize()
    configurations = listOf(project.configurations.shadow.get())
    val folder = System.getenv("pluginFolder_1-20")
    if (folder != null) destinationDirectory.set(file(folder))
    archiveFileName.set("${project.name}-${project.version}.jar")
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

// Handles version variables
tasks.processResources {
    expand(project.properties)

    inputs.property("version", rootProject.version)
    filesMatching("plugin.yml") {
        expand("version" to rootProject.version)
    }
}