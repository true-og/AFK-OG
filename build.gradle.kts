plugins {
    id("java")
    id("java-library")
    id("com.gradleup.shadow") version "8.3.9"
    eclipse
}

group = "de.codecrafter"
version = providers.gradleProperty("version").getOrElse("1.0.0-beta")

val pluginName = "AFK-OG"
val apiVersion = "1.19"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.GRAAL_VM)
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://repo.purpurmc.org/snapshots") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    maven { url = uri("file://${System.getProperty("user.home")}/.m2/repository") }
    System.getProperty("SELF_MAVEN_LOCAL_REPO")?.let {
        val dir = file(it)
        if (dir.isDirectory) {
            maven { url = uri("file://${dir.absolutePath}") }
        } else {
            mavenLocal()
        }
    } ?: mavenLocal()
}

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.12.2")
    implementation("com.google.code.gson:gson:2.13.2")
}

tasks.processResources {
    val props = mapOf("version" to version, "apiVersion" to apiVersion, "pluginName" to pluginName)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") { expand(props) }
    from("LICENSE") { into("/") }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.isFork = true
    options.release.set(17)
    options.compilerArgs.add("-parameters")
    options.compilerArgs.add("-Xlint:deprecation")
}

tasks.jar {
    archiveBaseName.set(pluginName)
    archiveClassifier.set("part")
}

tasks.shadowJar {
    archiveBaseName.set(pluginName)
    archiveClassifier.set("")
    archiveVersion.set(project.version.toString())
    archiveFileName.set("$pluginName-${project.version}.jar")
    isEnableRelocation = true
    relocationPrefix = "${project.group}.shadow"
    minimize()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
