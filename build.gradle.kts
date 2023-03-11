plugins {
    `java-library`
    `maven-publish`
    idea
    id("com.github.johnrengelman.shadow") version "8.1.0"
}

group = "com.pixelmongenerations"
version = "1.2.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

sourceSets {
    val assetLoading = create("assetLoading") {
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
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")

    shadow(implementation("org.tukaani", "xz", "1.9"))
    shadow(implementation("org.apache.commons", "commons-compress", "1.22"))
    shadow(implementation("org.joml", "joml", "1.10.5"))
    shadow(implementation("de.javagl", "jgltf-model", "2.0.3"))

    shadow(implementation(platform("org.lwjgl:lwjgl-bom:3.3.1"))!!)
    shadow(implementation("org.lwjgl", "lwjgl"))
    shadow(implementation("org.lwjgl", "lwjgl-glfw"))
    shadow(implementation("org.lwjgl", "lwjgl-opengl"))
    shadow(implementation("org.lwjgl", "lwjgl-stb"))
    shadow(implementation("com.github.thecodewarrior", "BinarySMD", "-SNAPSHOT"))

    shadow(runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-windows"))
    shadow(runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = "natives-windows"))
    shadow(runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = "natives-windows"))
    shadow(runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = "natives-windows"))

    shadow(implementation("org.slf4j:slf4j-jdk14:2.0.6")!!)

    // PokeUtils Libs
    shadow(implementation("com.github.weisj:darklaf-core:3.0.2")!!)
    shadow(implementation("com.intellij:forms_rt:7.0.3")!!)
    shadow(implementation("org.lwjgl", "lwjgl-nfd"))
    shadow(runtimeOnly("org.lwjgl", "lwjgl-nfd", classifier = "natives-windows"))
    shadow(implementation("org.lwjglx", "lwjgl3-awt", "0.1.8"))

    shadow(implementation("com.google.flatbuffers:flatbuffers-java:23.3.3")!!)
}

tasks {
    shadowJar {
        archiveBaseName.set("RareCandyTools")
        from(sourceSets.getByName("assetLoading").output.classesDirs)
        from(sourceSets.getByName("renderer").output.classesDirs)
        from(sourceSets.getByName("renderer").output.resourcesDir)
        manifest {
            attributes(mapOf("Main-Class" to "com.pokemod.rarecandy.tools.Main"))
        }
    }

    jar {
        from(sourceSets.getByName("assetLoading").output.classesDirs)
        from(sourceSets.getByName("renderer").output.classesDirs)
        from(sourceSets.getByName("renderer").output.resourcesDir)
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        dependsOn("processAssetLoadingResources")
        dependsOn("processRendererResources")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
        mavenLocal()
        maven {
            /*val releasesRepoUrl = "https://maven.pixelmongenerations.com/repository/maven-releases/"
            val snapshotsRepoUrl = "https://maven.pixelmongenerations.com/repository/maven-snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT") || version.toString().startsWith("0")) snapshotsRepoUrl else releasesRepoUrl)*/
            name = "PokeModRepo"
            url = uri("https://maven.pixelmongenerations.com/repository/maven-private/")
            credentials {
                username = project.properties["repoLogin"]?.toString() ?: findProperty("REPO_LOGIN").toString()
                password = project.properties["repoPassword"]?.toString() ?: findProperty("REPO_PASSWORD").toString()
            }
        }

    }
}

fun mcDependency(handler: DependencyHandlerScope, group: String, name: String) {
    handler.compileOnly("$group:$name")
    handler.testImplementation("$group:$name")
}
