plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.pixelmongenerations"
version = "1.0-SNAPSHOT"
val rootPkg = "com.pixelmongenerations"

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

    addNative(this, "org.lwjgl", "lwjgl")
    addNative(this, "org.lwjgl", "lwjgl-stb")
    addNative(this, "org.lwjgl", "lwjgl-glfw")
    addNative(this, "org.lwjgl", "lwjgl-opengl")
    addNative(this, "org.lwjgl", "lwjgl-assimp")
}

// We Exclude 32-bit systems because they are old.
fun addNative(handler: DependencyHandlerScope, group: String, name: String) {
    handler.implementation(group, name, classifier = "natives-windows")
    handler.implementation(group, name, classifier = "natives-windows-arm64")
    handler.implementation(group, name, classifier = "natives-linux")
    handler.implementation(group, name, classifier = "natives-linux-arm64")
    handler.implementation(group, name, classifier = "natives-macos")
    handler.implementation(group, name, classifier = "natives-macos-arm64")
}
