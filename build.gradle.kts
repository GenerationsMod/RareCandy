import org.gradle.internal.os.OperatingSystem
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "cf.hydos"
version = "1.0-SNAPSHOT"
val rootPkg = "com.base.game"

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

    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-assimp", classifier = lwjglNatives)
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