import org.gradle.internal.os.OperatingSystem
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "cf.hydos"
version = "1.0-SNAPSHOT"
val rootPkg = "cf.hydos"

val lwjglNatives = when (OperatingSystem.current()) {
    OperatingSystem.LINUX -> System.getProperty("os.arch").let {
        if (it.startsWith("arm") || it.startsWith("aarch64")) "natives-linux-${if (it.contains("64") || it.startsWith("armv8")) "arm64" else "arm32"}"
        else "natives-linux"
    }
    OperatingSystem.MAC_OS -> "natives-macos"
    OperatingSystem.WINDOWS -> System.getProperty("os.arch").let {
        if (it.contains("64")) "natives-windows${if (it.startsWith("aarch64")) "-arm64" else ""}"
        else "natives-windows-x86"
    }
    else -> throw Error("Unrecognized or unsupported Operating system. Please set \"lwjglNatives\" manually")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains", "annotations", "23.0.0")

    implementation("org.tukaani", "xz", "1.9")
    implementation("org.apache.commons", "commons-compress", "1.21")
    implementation("org.joml", "joml", "1.10.3")
    implementation("com.intellij", "forms_rt", "7.0.3")

    implementation(platform("org.lwjgl:lwjgl-bom:3.3.0"))
    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-stb")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-opengl")
    implementation("org.lwjgl", "lwjgl-assimp")

    addEveryOsNative(this, "org.lwjgl", "lwjgl")
    addEveryOsNative(this, "org.lwjgl", "lwjgl-stb")
    addEveryOsNative(this, "org.lwjgl", "lwjgl-glfw")
    addEveryOsNative(this, "org.lwjgl", "lwjgl-opengl")
    addEveryOsNative(this, "org.lwjgl", "lwjgl-assimp")
}

// We Exclude 32-bit systems because they are old.
fun addEveryOsNative(handler: DependencyHandlerScope, group: String, name: String) {
    handler.implementation(group, name, classifier = "natives-windows")
    handler.implementation(group, name, classifier = "natives-windows-arm64")
    handler.implementation(group, name, classifier = "natives-linux")
    handler.implementation(group, name, classifier = "natives-linux-arm64")
    handler.implementation(group, name, classifier = "natives-macos")
    handler.implementation(group, name, classifier = "natives-macos-arm64")
}

tasks {
    named<ShadowJar>("shadowJar") {

        manifest {
            attributes(mapOf("Main-Class" to "$rootPkg.Main"))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}