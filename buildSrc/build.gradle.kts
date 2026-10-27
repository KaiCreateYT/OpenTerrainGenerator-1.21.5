plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(21)
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("com.gradleup.shadow", "shadow-gradle-plugin", "8.3.8")
}
