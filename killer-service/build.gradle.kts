plugins {
    id("java")
    id("war")
}

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    providedCompile("jakarta.platform:jakarta.jakartaee-api:10.0.0")
    implementation("org.springframework:spring-webmvc:6.2.9")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.18.4")
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
}

tasks.withType<JavaCompile> {
    options.release.set(17)
}

tasks.war {
    archiveFileName.set("killer-service.war")
}
