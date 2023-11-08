plugins {
    java
    `java-library`
    `maven-publish`
    idea
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "gg.generations"
version = "2.4.8-SNAPSHOT"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

sourceSets {
    val assetLoading = create("library") {
        compileClasspath += main.get().compileClasspath
    }

    val rendering = create("renderer") {
        compileClasspath += main.get().compileClasspath + assetLoading.output
    }

    main {
        this.compileClasspath += assetLoading.output + rendering.output
        this.runtimeClasspath += assetLoading.output + rendering.output
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://maven.generations.gg/releases")
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")

    shadow(implementation("org.tukaani", "xz", "1.9"))
    shadow(implementation("org.apache.commons", "commons-compress", "1.23.0"))
    shadow(implementation("org.joml", "joml", "1.10.5"))
    shadow(implementation("de.javagl", "jgltf-model", "2.0.3"))

    shadow(implementation(platform("org.lwjgl:lwjgl-bom:3.3.2"))!!)
    shadow(implementation("org.lwjgl", "lwjgl"))
    shadow(implementation("org.lwjgl", "lwjgl-glfw"))
    shadow(implementation("org.lwjgl", "lwjgl-opengl"))
    shadow(implementation("org.lwjgl", "lwjgl-stb"))
    shadow(implementation("com.github.thecodewarrior", "BinarySMD", "-SNAPSHOT"))

    shadow(runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-windows"))
    shadow(runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = "natives-windows"))
    shadow(runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = "natives-windows"))
    shadow(runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = "natives-windows"))

    shadow(implementation("org.slf4j:slf4j-jdk14:2.0.7")!!)

    // PokeUtils Libs
    shadow(implementation("com.github.weisj:darklaf-core:3.0.2")!!)
    shadow(implementation("com.intellij:forms_rt:7.0.3")!!)
    shadow(implementation("org.lwjgl", "lwjgl-nfd"))
    shadow(runtimeOnly("org.lwjgl", "lwjgl-nfd", classifier = "natives-windows"))
    shadow(implementation("org.lwjglx", "lwjgl3-awt", "0.1.8"))

    shadow(implementation("com.google.flatbuffers:flatbuffers-java:23.5.26")!!)

    //TODO: JT need some funky gradle logic that lets us build a version does and doesn't include gson for the viewer and generations respectively
    shadow(implementation("com.google.code.gson:gson:2.10.1")!!)
//    shadow(implementation("de.javagl:jgltf-model-builder:2.0.4-SNAPSHOT")!!)


    shadow(implementation("com.thebombzen:jxlatte:1.1.2")!!)
}

tasks {
    shadowJar {
        archiveBaseName.set("RareCandyTools")
        from(sourceSets.getByName("library").output.classesDirs)
        from(sourceSets.getByName("library").output.resourcesDir)
        manifest.attributes(mapOf("Main-Class" to "gg.generations.rarecandy.tools.Main"))
    }

    jar {
        from(sourceSets.getByName("library").output.classesDirs)
        from(sourceSets.getByName("library").output.resourcesDir)
    }

    build.get().dependsOn(shadowJar)

    processResources {
        dependsOn("processLibraryResources")
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

fun mcDependency(handler: DependencyHandlerScope, group: String, name: String) {
    handler.compileOnly("$group:$name")
    handler.testImplementation("$group:$name")
}
