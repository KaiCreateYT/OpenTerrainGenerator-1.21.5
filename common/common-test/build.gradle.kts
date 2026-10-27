plugins {
    java
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(project(":common:common-core"))
    implementation(project(":common:common-util"))

    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")
    implementation("it.unimi.dsi:fastutil:8.5.12")
    implementation("com.google.guava:guava:32.1.3-jre")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
}

application {
    mainClass.set("com.pg85.otg.test.cli.SnapshotCli")
}
