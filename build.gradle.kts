import org.gradle.jvm.tasks.Jar

plugins {
    kotlin("jvm") version "2.2.21"
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.toVersion(17)
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
    jcenter()
}

group = "com.github.szantogab"
version = "1.1.22"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains", "annotations", "15.0")
    implementation("net.sf.kxml", "kxml2", "2.3.0")
    api("io.reactivex.rxjava3", "rxjava", "3.1.12")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.test {
    useJUnitPlatform()
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
