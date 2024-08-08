import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    `java-library`
    `maven-publish`
    idea
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "gg.generations"
version = "2.10"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

sourceSets {
    val assetLoading = create("library") {
        compileClasspath += main.get().compileClasspath
    }

    main {
        this.compileClasspath += assetLoading.output
        this.runtimeClasspath += assetLoading.output
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://maven.generations.gg/releases")
}

dependencies {
    configurations {
        create("shadowTools")
    }

    compileOnly("org.jetbrains:annotations:24.1.0")

    "shadow"(implementation("org.tukaani", "xz", "1.9"))
    "shadowTools"(implementation("org.apache.commons", "commons-compress", "1.26.1"))
    "shadowTools"(implementation("org.joml", "joml", "1.10.5"))
    "shadowTools"(implementation("de.javagl", "jgltf-model", "2.0.5"))

    "shadowTools"(implementation(platform("org.lwjgl:lwjgl-bom:3.3.2"))!!)
    "shadowTools"(implementation("org.lwjgl", "lwjgl"))
    "shadowTools"(implementation("org.lwjgl", "lwjgl-glfw"))
    "shadowTools"(implementation("org.lwjgl", "lwjgl-opengl"))
    "shadowTools"(implementation("org.lwjgl", "lwjgl-stb"))
    "shadow"(implementation("org.lwjgl", "lwjgl-assimp", "3.3.2"))
    "shadow"(implementation("com.github.thecodewarrior", "BinarySMD", "-SNAPSHOT"))

    "shadowTools"(runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-windows"))
    "shadowTools"(runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = "natives-windows"))
    "shadowTools"(runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = "natives-windows"))
    "shadowTools"(runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = "natives-windows"))
    "shadow"(runtimeOnly("org.lwjgl", "lwjgl-assimp", classifier = "natives-windows"))

    "shadowTools"(implementation("org.slf4j:slf4j-jdk14:2.0.12")!!)

    // PokeUtils Libs
    "shadowTools"(implementation("com.github.weisj:darklaf-core:3.0.2")!!)
    "shadowTools"(implementation("com.intellij:forms_rt:7.0.3")!!)
    "shadowTools"(implementation("org.lwjgl", "lwjgl-nfd"))
    "shadowTools"(runtimeOnly("org.lwjgl", "lwjgl-nfd", classifier = "natives-windows"))
    "shadowTools"(implementation("org.lwjglx", "lwjgl3-awt", "0.1.8"))

    "shadow"(implementation("com.google.flatbuffers:flatbuffers-java:23.5.26")!!)

    //TODO: JT need some funky gradle logic that lets us build a version does and doesn't include gson for the viewer and generations respectively
    "shadowTools"(implementation("com.google.code.gson:gson:2.10.1")!!)
    "shadowTools"(implementation("de.javagl:jgltf-model-builder:2.0.5")!!)


}

tasks {
    var rareCandyTools = register<ShadowJar>("rare_candy_tools") {
        archiveBaseName.set("RareCandyTools")
        from(sourceSets["main"].output.resourcesDir)
        from(sourceSets["main"].output.classesDirs)
        from(sourceSets["library"].output.classesDirs)
        manifest.attributes(mapOf("Main-Class" to "gg.generations.rarecandy.tools.Main"))

        configurations = listOf(
            project.configurations.getByName("shadow"),
            project.configurations.getByName("shadowTools")
        )

        dependsOn("processResources")
    }

    var rareCandy = register<ShadowJar>("rare_candy") {
        archiveBaseName.set("RareCandy")
        from(sourceSets.getByName("library").output.classesDirs)
        exclude("src/main/**",
            "org/lwjgl/system//**",
            "org/lwjgl/BufferUtils.class",
            "org/lwjgl/CLongBuffer.class",
            "org/lwjgl/PointerBuffer.class",
            "org/lwjgl/Version\$BuildType.class",
            "org/lwjgl/Version.class",
            "org/lwjgl/VersionImpl.class",
            "org/lwjgl/package-info.class")

        relocate("org.lwjgl.assimp", "gg.generations.rarecandy.shaded.assimp")
        relocate("org.tukaani.xz", "gg.generations.rarecandy.shaded.xz")
        relocate("com.github.benmanes.caffeine.cache", "gg.generations.rarecandy.shaded.caffeine.cache")
        relocate("com.google.flatbuffers", "gg.generations.rarecandy.shaded.flatbuffers")
        relocate("org.msgpack", "gg.generations.rarecandy.shaded.msgpack")
        relocate("dev.thecodewarrior.binarysmd", "gg.generations.rarecandy.shaded.binarysmd")
        relocate("org.checkerframework", "gg.generations.rarecandy.shaded.checkerframework")
        relocate("com.google.errorprone", "gg.generations.rarecandy.shaded.errorprone")
        relocate("org.apache.commons", "gg.generations.rarecandy.shaded.commons")



        configurations = listOf(
            project.configurations.getByName("shadow")
        )
    }


    build.get().dependsOn(rareCandy.get(), rareCandyTools.get())
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
