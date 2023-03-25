import name.remal.gradle_plugins.dsl.extensions.applyPlugin
import name.remal.gradle_plugins.plugins.publish.ossrh.RepositoryHandlerOssrhExtension
import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    kotlin("jvm") version "1.7.22"

    id("io.gitlab.arturbosch.detekt") version "1.22.0"
    id("jacoco")

    id("net.researchgate.release") version "3.0.2"

    id("org.jetbrains.dokka") version "1.7.20"

    id("com.github.hierynomus.license") version "0.16.1"
    id("com.github.jk1.dependency-license-report") version "2.1"

    id("maven-publish")
    id("name.remal.maven-publish-ossrh") version "1.5.0" apply false
}

java.sourceCompatibility = VERSION_17
java.targetCompatibility = VERSION_17

tasks.compileKotlin.configure {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions"
        )
    }
}

tasks.withType(KotlinCompile::class.java) {
    kotlinOptions {
        jvmTarget = "$VERSION_17"
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.slf4j:slf4j-api:1.7.36")

    val junitVersion = "5.9.2"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.2.12")
}

detekt {
    source = files("src")
    config = files("detekt.yml")


}

tasks.detekt {
    reports {
        xml {
            required.set(true)
            outputLocation.set(file("$buildDir/reports/detekt.xml"))
        }
    }
}

license {
    excludePatterns = setOf(
        "**/*.json",
        "**/*.properties",
        "**/META-INF/**/*"
    )

    header = file("HEADER.txt")
    skipExistingHeaders = true

    ext {
        set("year", Calendar.getInstance().get(Calendar.YEAR))
    }
}

jacoco {
    toolVersion = "0.8.8"
}

tasks.test.configure {
    useJUnitPlatform()

    configure<JacocoTaskExtension> {
        setDestinationFile(file("$buildDir/jacoco/test.exec"))
    }
}

tasks.jacocoTestReport.get().dependsOn(tasks.check)
tasks.build.get().dependsOn(tasks.jacocoTestReport)

release {
    tagTemplate.set("v\$version")
}

tasks.jar.configure {
    manifest {
        val moduleNameRegex = Regex("""[^\w\.${"$"}_]""")

        attributes(
            "Automatic-Module-Name" to "${project.group}.${project.name}"
                .replace(moduleNameRegex, "_"),

            "Implementation-Version" to project.version,
            "Implementation-Title" to "Events for Kotlin"
        )
    }
}

tasks.dokkaJavadoc.configure {
    moduleName.set("v47.io Events")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaJavadoc)

    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc.get().outputDirectory)
}

publishing {
    publications {
        create("maven", MavenPublication::class) {
            groupId = "${project.group}"
            artifactId = project.name
            version = "${project.version}"

            from(components.getByName("java"))

            artifact(sourcesJar) {
                classifier = "sources"
            }

            artifact(javadocJar) {
                classifier = "javadoc"
            }

            pom {
                name.set("Events for Kotlin")
                description.set("Simple asynchronous events for Kotlin")
                url.set("https://github.com/v47-io/events")

                licenses {
                    license {
                        name.set("BSD 3-Clause License")
                        url.set("https://opensource.org/licenses/BSD-3-Clause")
                    }
                }

                developers {
                    developer {
                        id.set("vemilyus")
                        name.set("Alex Katlein")
                        email.set("dev@vemilyus.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/v47-io/events.git")
                    developerConnection.set("scm:git:git://github.com/v47-io/events.git")
                    url.set("https://github.com/v47-io/events")
                }
            }
        }
    }
}

val ossrhUser: String? = project.findProperty("ossrhUser") as? String ?: System.getenv("OSSRH_USER")
val ossrhPass: String? = project.findProperty("osshrPass") as? String ?: System.getenv("OSSRH_PASS")

if (!ossrhUser.isNullOrBlank() && !ossrhPass.isNullOrBlank() && !"${project.version}".endsWith("-SNAPSHOT")) {
    applyPlugin("signing")
    applyPlugin("name.remal.maven-publish-ossrh")

    publishing {
        repositories {
            withConvention(RepositoryHandlerOssrhExtension::class) {
                ossrh {
                    credentials {
                        username = ossrhUser
                        password = ossrhPass
                    }
                }
            }
        }
    }
}
