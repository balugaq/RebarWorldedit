import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    java
    idea
    id("com.gradleup.shadow") version "8.3.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("io.freefair.lombok") version "8.13.1"
}

group = project.properties["group"]!!

repositories {
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots/") {
        name = "sonatype"
    }
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
    maven("https://repo.metamechanists.org/releases") {
        name = "MetaMechanists Repository"
    }
    maven("https://jitpack.io") {
        name = "JitPack"
    }
    maven("https://repo.xenondevs.xyz/releases")
}

// Get dependency versions from gradle.properties
val rebarVersion = project.properties["rebar.version"] as String
val pylonVersion = project.properties["pylon.version"] as String

// Download dependencies
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("io.github.pylonmc:rebar:$rebarVersion")
    compileOnly("io.github.pylonmc:pylon:$pylonVersion")
    implementation("org.metamechanists:DisplayModelLib:35")
    shadow("org.metamechanists:DisplayModelLib:35")
}

// Settings for IntelliJ
idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

java {
    // Use Java 21
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

// Configuration for the output JAR
tasks.shadowJar {
    relocate("org.metamechanists", "${project.group}.shaded.org.metamechanists")

    mergeServiceFiles()

    archiveBaseName = project.name
    archiveClassifier = null
}

// Generate the plugin.yml file using the bukkit gradle plugin
bukkit {
    name = project.name
    main = project.properties["main-class"] as String
    version = project.version.toString()
    apiVersion = "1.21"
    depend = listOf("Rebar")
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    website = "https://github.com/balugaq/RebarWorldedit"
}

// Run a server using the run server gradle plugin
tasks.runServer {
    // Download pylon core and add it to the plugins folder
    downloadPlugins {
        github("pylonmc", "rebar", rebarVersion, "rebar-$rebarVersion.jar")
    }
    maxHeapSize = "4G"
    minecraftVersion("1.21.10")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}