import org.jreleaser.model.Active
import org.jreleaser.model.Signing

plugins {
    id("java-library")
    id("com.github.johnrengelman.shadow") version("8.1.1")
    id("io.freefair.lombok") version("8.11")

    id("maven-publish")
    id("org.jreleaser") version("1.16.0")
}

group = "dev.projectenhanced"
version = "1.0.0-SNAPSHOT"
description = "EnhancedSpigot is an advanced library that enhances the Spigot API for creating powerful Minecraft plugins"

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "io.freefair.lombok")

    apply(plugin = "maven-publish")
    apply(plugin = "org.jreleaser")

    group = "dev.projectenhanced"

    java {
        withSourcesJar()
        withJavadocJar()

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }


    tasks.shadowJar {
        archiveFileName.set("${project.name}-${project.version}.jar")
        finalizedBy(tasks.javadoc)
        finalizedBy(tasks.named("sourcesJar"))
    }

    publishing {
        repositories {
            maven {
                if (project.version.toString().endsWith("-SNAPSHOT")) {
                    credentials {
                        username = rootProject.findProperty("nexusUsername") as String?
                        password = rootProject.findProperty("nexusPassword") as String?
                    }

                    url = uri("https://nexus.projectenhanced.dev/repository/maven-snapshots")
                } else {
                    url =  uri(layout.buildDirectory.dir("staging-deploy").get())
                }
            }
        }

        publications {
            create<MavenPublication>("maven") {
                groupId = project.group as String?
                artifactId = project.name

                from(components["java"])

                pom {
                    name.set(project.name)
                    description.set(project.description)
                    url.set("https://projectenhanced.dev")
                    inceptionYear.set("2025")

                    licenses {
                        license {
                            name.set("Apache-2.0")
                            url.set("https://spdx.org/licenses/Apache-2.0.html")
                        }
                    }

                    developers {
                        developer {
                            id.set("kpgtb")
                            name.set("KPG-TB")
                        }
                    }

                    scm {
                        connection.set("scm:git:https://github.com/KPGTB/EnhancedSpigot.git")
                        developerConnection.set("scm:git:ssh://github.com/KPGTB/EnhancedSpigot.git")
                        url.set("https://github.com/KPGTB/EnhancedSpigot")
                    }
                }
            }
        }
    }

    jreleaser {
        release {
            github {
                skipRelease.set(true)
                skipTag.set(true)
            }
        }
        signing {
            active.set(Active.ALWAYS)
            armored.set(true)
            mode.set(Signing.Mode.FILE)
            publicKey.set(rootProject.findProperty("publicKey") as String?)
            secretKey.set(rootProject.findProperty("privateKey") as String?)
        }
        deploy {
            maven {
                mavenCentral {
                    create("sonatype") {
                        active.set(Active.ALWAYS)
                        url.set("https://central.sonatype.com/api/v1/publisher")
                        stagingRepository("build/staging-deploy")
                    }
                }
            }
        }
    }
}