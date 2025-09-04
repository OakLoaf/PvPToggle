import java.io.BufferedReader
import java.io.InputStreamReader

plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version("8.3.0")
    id("xyz.jpenilla.run-paper") version("2.2.4")
    id("com.modrinth.minotaur") version("2.+")
}

group = "org.lushplugins"
version = "3.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    maven("https://maven.enginehub.org/repo/") // WorldGuard
    maven("https://repo.xemor.zip/releases/") // EnchantedStorage
    maven("https://repo.lushplugins.org/snapshots/") // LushLib, PluginUpdater, PlaceholderHandler
}

dependencies {
    // Dependencies
    compileOnly("org.spigotmc:spigot-api:1.21.7-R0.1-SNAPSHOT")

    // Soft Dependencies
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9")

    // Libraries
    implementation("org.lushplugins:LushLib:0.10.82")
    implementation("org.enchantedskies:EnchantedStorage:3.0.0")
    implementation("io.github.revxrsal:lamp.common:4.0.0-rc.12")
    implementation("io.github.revxrsal:lamp.bukkit:4.0.0-rc.12")
    implementation("org.lushplugins:PlaceholderHandler:1.0.0-alpha6")
    implementation("org.lushplugins.pluginupdater:PluginUpdater-API:1.0.3")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))

    registerFeature("optional") {
        usingSourceSet(sourceSets["main"])
    }

    withSourcesJar()
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    shadowJar {
        relocate("org.lushplugins.lushlib", "org.lushplugins.pvptoggle.libraries.lushlib")
        relocate("org.enchantedskies", "org.lushplugins.pvptoggle.libraries.enchantedstorage")
        relocate("org.lushplugins.placeholderhandler", "org.lushplugins.pvptoggle.libraries.placeholderhandler")
        relocate("org.lushplugins.pluginupdater", "org.lushplugins.pvptoggle.libraries.pluginupdater")

        minimize()

        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    processResources{
        filesMatching("plugin.yml") {
            expand(project.properties)
        }

        inputs.property("version", rootProject.version)
        filesMatching("plugin.yml") {
            expand("version" to rootProject.version)
        }
    }

    runServer {
        minecraftVersion("1.21.7")

        downloadPlugins {
            hangar("PlaceholderAPI", "2.11.6")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "lushReleases"
            url = uri("https://repo.lushplugins.org/releases")
            credentials(PasswordCredentials::class)
            authentication {
                isAllowInsecureProtocol = true
                create<BasicAuthentication>("basic")
            }
        }

        maven {
            name = "lushSnapshots"
            url = uri("https://repo.lushplugins.org/snapshots")
            credentials(PasswordCredentials::class)
            authentication {
                isAllowInsecureProtocol = true
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = rootProject.name
            version = rootProject.version.toString()
            from(project.components["java"])
        }
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("82jcLX4b")
    if (System.getenv("RELEASE_TYPE") == "release") {
        versionNumber.set(rootProject.version.toString())
        changelog.set(getChangelogSinceLastTag())
    } else {
        versionNumber.set("${rootProject.version}-${getCurrentCommitHash()}")
    }
    uploadFile.set(file("build/libs/${project.name}-${project.version}.jar"))
    versionType.set(System.getenv("RELEASE_TYPE"))
    gameVersions.addAll(
        "1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4", "1.20.5", "1.20.6",
        "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8"
    )
    loaders.addAll("spigot", "paper", "purpur")
//    syncBodyFrom.set(rootProject.file("README.md").readText()) // TODO: Add README
}

tasks.modrinth {
    dependsOn("shadowJar")
    dependsOn(tasks.modrinthSyncBody)
}

fun getCurrentCommitHash(): String {
    val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD").start()
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val commitHash = reader.readLine()
    reader.close()
    process.waitFor()
    if (process.exitValue() == 0) {
        return commitHash ?: ""
    } else {
        throw IllegalStateException("Failed to retrieve the commit hash.")
    }
}

fun getLastTag(): String {
    return ProcessBuilder("git", "describe", "--tags", "--abbrev=0")
        .start().inputStream.bufferedReader().readText().trim()
}

fun getChangelogSinceLastTag(): String {
    return ProcessBuilder("git", "log", "${getLastTag()}..HEAD", "--pretty=format:* %s ([#%h](https://github.com/OakLoaf/${rootProject.name}/commit/%H))")
        .start().inputStream.bufferedReader().readText().trim()
}