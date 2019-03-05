import groovy.lang.Closure

buildscript {
    dependencies {
        repositories {
            mavenCentral()
            maven {
                setUrl("https://plugins.gradle.org/m2/")
            }
        }
    }
}

plugins {
    kotlin("jvm") version "1.3.11"
    `java-library`
    `maven-publish`
    maven
    signing
    `build-scan`
    id("org.jetbrains.dokka") version "0.9.17"
    id("com.palantir.git-version") version "0.12.0-rc2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin", version = "1.3.11")
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8", version = "1.3.11")
    implementation(group = "de.undercouch", name = "gradle-download-task", version = "3.4.3")
    implementation(group = "org.apache.commons", name = "commons-lang3", version = "3.8.1")
    implementation(group = "com.google.guava", name = "guava", version = "26.0-jre")

    api(gradleApi())
}

val gitVersion: Closure<String> by extra

version = gitVersion()

group = "ch.leadrian.samp.kamp"

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.dokka)
    archiveClassifier.set("javadoc")
}

tasks {
    compileKotlin {
        sourceCompatibility = "1.8"
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    dokka {
        reportUndocumented = false
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name.set("Kamp Gradle Plugins")
                description.set("Gradle plugins for Kamp")
                url.set("https://github.com/Double-O-Seven/kamp-server-starter")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("Double-O-Seven")
                        name.set("Adrian-Philipp Leuenberger")
                        email.set("thewishwithin@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/Double-O-Seven/kamp-server-starter.git")
                    developerConnection.set("scm:git:ssh://github.com/Double-O-Seven/kamp-server-starter.git")
                    url.set("https://github.com/Double-O-Seven/kamp-server-starter")
                }
            }
        }
    }
    repositories {
        maven {
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            url = if (version.toString().contains("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                val ossrhUsername: String? by extra
                val ossrhPassword: String? by extra
                username = ossrhUsername
                password = ossrhPassword
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}
