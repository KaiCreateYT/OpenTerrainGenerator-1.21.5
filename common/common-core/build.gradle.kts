description = "Core files of OpenTerrainGenerator"

dependencies {
    api(project(":common:common-util"))
    api(project(":common:common-customobject"))
    api(project(":common:common-generator"))

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
    testImplementation(project(":common:common-test"))
}

tasks.test {
    useJUnitPlatform()
    workingDir = rootProject.projectDir
}
