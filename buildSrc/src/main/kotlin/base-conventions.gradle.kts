plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    // Shaded dependencies
    api("com.fasterxml.jackson.core:jackson-annotations:2.17.1")
    api("com.fasterxml.jackson.core:jackson-core:2.17.1")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.1")
    api("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    api("com.fasterxml.jackson.module:jackson-module-jsonSchema:2.17.1")
    api("org.yaml:snakeyaml:2.2")
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
}

val javaVersion = project.property("java_version").toString().toInt()
configure<JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
    withSourcesJar()
}

configure<BasePluginExtension> {
    version = if (project.property("otg_build").toString() == "") {
        project.property("otg_version").toString()
    } else {
        project.property("otg_version").toString() + "-SNAPSHOT"
    }
    group = project.property("otg_group").toString()
}

configure<PublishingExtension> {
    publications.create<MavenPublication>("maven") {
        artifactId = "otg-${project.name}"
        from(components["java"])
    }
}

tasks {
    withType<JavaCompile> {
        options.release.set(javaVersion)
        options.encoding = Charsets.UTF_8.name()
    }

    withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }

    withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
}
