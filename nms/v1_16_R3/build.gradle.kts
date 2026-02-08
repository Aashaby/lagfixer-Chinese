plugins {
    id("java")
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.16.5-R0.1-SNAPSHOT")
    compileOnly(project(":plugin"))
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
}