plugins {
    `java`
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

dependencies {
    implementation(project(":library"))
    // Add additional dependencies for the tools module
}

tasks.shadowJar {
    manifest.attributes["Main-Class"] = "com.example.MainClassName" // Specify the main class here
}