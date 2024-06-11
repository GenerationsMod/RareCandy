plugins {
    `java`
    `maven-publish`
}

group = "com.example"
version = "1.0.0"

repositories {
    // Add repositories if needed
}

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://jitpack.io")
        maven("https://maven.generations.gg/releases")
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:24.1.0")

        implementation("org.tukaani", "xz", "1.9")
        implementation("org.apache.commons", "commons-compress", "1.26.1")
        implementation("org.joml", "joml", "1.10.5")
        implementation("de.javagl", "jgltf-model", "2.0.5")

        implementation(platform("org.lwjgl:lwjgl-bom:3.3.2"))!!
        implementation("org.lwjgl", "lwjgl")
        implementation("org.lwjgl", "lwjgl-glfw")
        implementation("org.lwjgl", "lwjgl-opengl")
        implementation("org.lwjgl", "lwjgl-stb")
        implementation("org.lwjgl", "lwjgl-assimp")
        implementation("com.github.thecodewarrior", "BinarySMD", "-SNAPSHOT")

        runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-windows")
        runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = "natives-windows")
        runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = "natives-windows")
        runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = "natives-windows")
        runtimeOnly("org.lwjgl", "lwjgl-assimp", classifier = "natives-windows")

        implementation("org.slf4j:slf4j-jdk14:2.0.12")

        // PokeUtils Libs
        implementation("com.github.weisj:darklaf-core:3.0.2")
        implementation("com.intellij:forms_rt:7.0.3")
        implementation("org.lwjgl", "lwjgl-nfd")
        runtimeOnly("org.lwjgl", "lwjgl-nfd", classifier = "natives-windows")
        implementation("org.lwjglx", "lwjgl3-awt", "0.1.8")

        implementation("com.google.flatbuffers:flatbuffers-java:23.5.26")

        //TODO: JT need some funky gradle logic that lets us build a version does and doesn't include gson for the viewer and generations respectively
        implementation("com.google.code.gson:gson:2.10.1")
        implementation("de.javagl:jgltf-model-builder:2.0.5")


    }

}

publishing {
    publications.create<MavenPublication>("maven").from(components["java"])
    repositories {
        mavenLocal()
        maven {
            val releasesRepoUrl = "https://maven.generations.gg/releases"
            val snapshotsRepoUrl = "https://maven.generations.gg/snapshots"
            url = uri(if (version.toString().endsWith("SNAPSHOT") || version.toString().startsWith("0")) snapshotsRepoUrl else releasesRepoUrl)
            name = "Generations-Repo"
            credentials {
                username = project.properties["repoLogin"]?.toString()
                password = project.properties["repoPassword"]?.toString()
            }
        }
    }
}
