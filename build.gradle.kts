plugins {
    `java-library`
    `maven-publish`
}

group = "com.pixelmongenerations"
version = "0.9.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.jetbrains", "annotations", "23.1.0")

    implementation("org.tukaani", "xz", "1.9")
    implementation("org.apache.commons", "commons-compress", "1.22")
    implementation("org.joml", "joml", "1.10.5")
    implementation("de.javagl", "jgltf-model", "2.0.3")

    implementation(platform("org.lwjgl:lwjgl-bom:3.3.1"))
    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-opengl")
    implementation("com.github.thecodewarrior", "BinarySMD", "-SNAPSHOT")

    runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-windows")
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = "natives-windows")
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = "natives-windows")

    compileOnly("org.slf4j:slf4j-api:2.0.5")
    testImplementation("org.slf4j:slf4j-jdk14:2.0.5")

    // PokeUtils Libs
    implementation("com.github.weisj:darklaf-core:3.0.2")
    implementation("com.intellij:forms_rt:7.0.3")
    implementation("org.lwjgl", "lwjgl-nfd")
    runtimeOnly("org.lwjgl", "lwjgl-nfd", classifier = "natives-windows")
    implementation("org.lwjglx", "lwjgl3-awt", "0.1.8")

    implementation("com.google.flatbuffers:flatbuffers-java:23.1.4")
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
            //val releasesRepoUrl = "https://maven.pixelmongenerations.com/repository/maven-releases/"
            //val snapshotsRepoUrl = "https://maven.pixelmongenerations.com/repository/maven-snapshots/"
            //url = uri(if (version.toString().endsWith("SNAPSHOT") || version.toString().startsWith("0")) snapshotsRepoUrl else releasesRepoUrl)
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
