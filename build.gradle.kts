plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.0"
}

val spigotRepo = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
val paperRepo = "https://repo.papermc.io/repository/maven-public/"
val sonatypeRepo = "https://oss.sonatype.org/content/groups/public/"
val jitpack = "https://jitpack.io"
val mojang = "https://libraries.minecraft.net"

version = "1.5.1.9"
extra["lagfixer_version"] = version
extra["lagfixer_build"] = "140"

dependencies {
    implementation(project(":plugin"))

    implementation(project(":nms:v1_16_R3"))
    implementation(project(":nms:v1_17_R1"))
    implementation(project(":nms:v1_18_R2"))
    implementation(project(":nms:v1_19_R3"))
    implementation(project(":nms:v1_20_R1"))
    implementation(project(":nms:v1_20_R2"))
    implementation(project(":nms:v1_20_R3"))
    implementation(project(":nms:v1_20_R4"))
    implementation(project(":nms:v1_21_R1"))
    implementation(project(":nms:v1_21_R2"))
    implementation(project(":nms:v1_21_R3"))
    implementation(project(":nms:v1_21_R4"))
    implementation(project(":nms:v1_21_R5"))
    implementation(project(":nms:v1_21_R6"))
    implementation(project(":nms:v1_21_R7"))

    implementation(project(":support:common"))
    implementation(project(":support:spigot"))
    implementation(project(":support:paper"))
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveBaseName.set("LagFixer")
        archiveClassifier.set("")
        archiveVersion.set("")

        relocate("net.kyori", "xyz.lychee.lagfixer.libs.kyori")
    }
}

allprojects {
    group = "xyz.lychee"

    apply(plugin = "java")

    repositories {
        maven("https://maven.aliyun.com/repository/public")
        mavenLocal()
        mavenCentral()
        maven(spigotRepo)
        maven(paperRepo)
        maven(sonatypeRepo)
        maven(mojang)
        maven(jitpack)
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.32")
        annotationProcessor("org.projectlombok:lombok:1.18.32")
    }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
        }
    }
}

subprojects {
    plugins.withId("java") {
        val isLegacyNms = project.path.startsWith(":nms:v1_16")
        val isJava21Nms = project.path.startsWith(":nms:v1_20_R4") || project.path.startsWith(":nms:v1_21")
        val toolchainVersion = when {
            isLegacyNms -> 17
            isJava21Nms -> 21
            else -> 17
        }
        val releaseVersion = when {
            isLegacyNms -> 8
            isJava21Nms -> 21
            else -> 17
        }

        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(toolchainVersion))
            }
            sourceCompatibility = when {
                isLegacyNms -> JavaVersion.VERSION_1_8
                isJava21Nms -> JavaVersion.VERSION_21
                else -> JavaVersion.VERSION_17
            }
            targetCompatibility = when {
                isLegacyNms -> JavaVersion.VERSION_1_8
                isJava21Nms -> JavaVersion.VERSION_21
                else -> JavaVersion.VERSION_17
            }
        }

        if (isLegacyNms || isJava21Nms) {
            configurations.matching { it.name.endsWith("Classpath") }.configureEach {
                attributes {
                    attribute(
                        TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE,
                        if (isJava21Nms) 21 else 17
                    )
                }
            }
        }

        tasks.withType<JavaCompile>().configureEach {
            options.release.set(releaseVersion)
        }
    }
}