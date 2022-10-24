plugins {
    `java-library`
    `maven-publish`
}

group = "com.pixelmongenerations"
version = "0.7.2"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains", "annotations", "23.0.0")

    implementation("org.tukaani", "xz", "1.9")
    implementation("org.apache.commons", "commons-compress", "1.21")
    implementation("org.joml", "joml", "1.10.5")
    implementation("de.javagl", "jgltf-model", "2.0.3")

    implementation(platform("org.lwjgl:lwjgl-bom:3.3.1"))
    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-opengl")

    runtimeOnly("org.lwjgl", "lwjgl" , classifier = "natives-windows")
    runtimeOnly("org.lwjgl", "lwjgl-glfw" , classifier = "natives-windows")
    runtimeOnly("org.lwjgl", "lwjgl-opengl" , classifier = "natives-windows")

    compileOnly("org.slf4j:slf4j-api:2.0.3")
    testImplementation("org.slf4j:slf4j-jdk14:2.0.3")
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
            url = uri("https://maven.pixelmongenerations.com/repository/maven-private/")
            println(project.properties["repoLogin"])
            credentials{
                username = project.properties["repoLogin"]?.toString() ?: findProperty("REPO_LOGIN").toString()
                password = project.properties["repoPassword"]?.toString() ?: findProperty("REPO_PASSWORD").toString()
            }
        }

    }
}

fun mcDependency(handler: DependencyHandlerScope, group: String, name: String) {
    handler.compileOnly(group, name)
    handler.testImplementation(group, name)
}
