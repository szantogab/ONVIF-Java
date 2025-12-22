import org.gradle.jvm.tasks.Jar

plugins {
    kotlin("jvm") version "2.2.21"
    `maven-publish`
}

repositories {
    mavenCentral()
    jcenter()
}

group = "be.teletask.onvif"
version = "1.1.15"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains", "annotations", "15.0")
    implementation("net.sf.kxml", "kxml2", "2.3.0")
    implementation("com.squareup.okhttp3", "okhttp", "4.9.3")
    implementation("io.github.rburgst", "okhttp-digest", "2.7")
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.9.0")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

artifacts.add("archives", sourcesJar)

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/szantogab/ONVIF-Java")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        register("gprRelease", MavenPublication::class) {
            groupId = "com.github.szantogab"
            artifactId = "onvif-java"
            version = project.version as String

            from(components["java"])

            artifact(sourcesJar)

            pom {
                packaging = "jar"
                name.set("ONVIF-Java")
                description.set("ONVIF support for Java and Kotlin")
                url.set("https://github.com/szantogab/ONVIF-Java")
                /*scm {
                    url.set(myGithubHttpUrl)
                }
                issueManagement {
                    url.set(myGithubIssueTrackerUrl)
                }*/
/*                licenses {
                    license {
                        name.set(myLicense)
                        url.set(myLicenseUrl)
                    }
                }*/
                developers {
                    developer {
                        id.set("szantogab")
                        name.set("Gabor Szanto")
                    }
                }
            }

        }
    }
}
