plugins {
    id("java")
    application
    id("com.gradleup.shadow") version("8.3.6")
}

group = "me.xemor"
version = "1.0-SNAPSHOT"

application {
    mainClass = "me.xemor.yaggaskabble.Main"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("net.dv8tion:JDA:5.3.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.18.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.pocketcombats:openskill:1.0")
}

tasks.test {
    useJUnitPlatform()
}