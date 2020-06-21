plugins {
    java
    kotlin("jvm") version "1.3.41"
    id("org.ajoberstar.grgit") version "4.0.2"
    id("com.palantir.docker") version "0.25.0"
}

repositories {
    jcenter()
}

version = "0.4.0+" + grgit.head().abbreviatedId

dependencies {
    // kotlin runtime
    compile(group = "org.jetbrains.kotlin", name = "kotlin-stdlib", version = "1.3.41")
    compile(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8", version = "1.3.41")
    compile(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = "1.3.41")
    compile(group = "org.jetbrains", name = "annotations", version = "17.0.0")

    // jackson, for parsing json
    compile(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.10.0")
    // jsoup, for parsing HTML
    compile(group = "org.jsoup", name = "jsoup", version = "1.12.1")
    // javalin, for wrapping servlet and serving content
    compile(group = "io.javalin", name = "javalin", version = "3.5.0")
    // jedis, for bridging redis.
    compile(group = "redis.clients", name = "jedis", version = "3.1.0")
    // jopt-simple, for parsing cli arguments
    compile(group = "net.sf.jopt-simple", name = "jopt-simple", version = "6.0-alpha-3")
    // slf4j, with java.util.logging binding
    compile(group = "org.slf4j", name = "slf4j-jdk14", version = "1.7.27")
}

tasks.register<Jar>("shadeJar") {
    dependsOn(":jar")
    classifier = "shaded"
    from(sourceSets["main"].output)
    // https://github.com/gradle/kotlin-dsl-samples/issues/1082
    from(configurations["compile"].map {
        if (it.isDirectory) it else zipTree(it).matching {
            exclude("META-INF/", "*.class", "*.css", "*.html")
        }
    })
}

artifacts {
    add("archives", tasks["shadeJar"])
}

docker {
    dependsOn(tasks["shadeJar"])

    name = "historia-in-speculo:${version}"
    // kotlin property setter refuse to work here
    setDockerfile(file("Dockerfile"))
    files(tasks["shadeJar"].outputs)
    buildArgs(mapOf("VERSION" to version.toString()))
}