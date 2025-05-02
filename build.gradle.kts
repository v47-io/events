import com.vanniktech.maven.publish.SonatypeHost
import java.net.URI
import java.util.*
import java.util.Calendar.YEAR

plugins {
    kotlin("jvm") version "2.1.10"

    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("jacoco")

    id("net.researchgate.release") version "3.1.0"

    id("org.jetbrains.dokka") version "2.0.0"
    id("org.jetbrains.dokka-javadoc") version "2.0.0"

    id("com.github.hierynomus.license") version "0.16.1"
    id("com.github.jk1.dependency-license-report") version "2.4"

    id("com.vanniktech.maven.publish") version "0.31.0"
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        freeCompilerArgs = listOf(
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions"
        )
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.slf4j:slf4j-api:2.0.17")

    val junitVersion = "5.12.2"
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.5.18")
}

tasks.detekt {
    reports {
        xml {
            required.set(true)
            outputLocation.set(file("${project.layout.buildDirectory.get()}/reports/detekt.xml"))
        }
    }
}

license {
    excludePatterns = setOf(
        "**/*.json",
        "**/*.properties",
        "**/META-INF/**/*"
    )

    mapping("java", "SLASHSTAR_STYLE")

    header = file("HEADER.txt")
    skipExistingHeaders = true

    ext {
        set("year", Calendar.getInstance().get(YEAR))
    }
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.test.configure {
    useJUnitPlatform()

    configure<JacocoTaskExtension> {
        setDestinationFile(file("${project.layout.buildDirectory.get()}/jacoco/test.exec"))
    }
}

tasks.jacocoTestReport.get().dependsOn(tasks.check)
tasks.build.get().dependsOn(tasks.jacocoTestReport)

release {
    tagTemplate.set("v\$version")
}

tasks.jar.configure {
    manifest {
        attributes(
            "Implementation-Version" to project.version,
            "Implementation-Title" to "Events for Kotlin"
        )
    }
}

dokka {
    dokkaSourceSets.main {
        includes.from("$projectDir/docs.md")

        sourceLink {
            localDirectory.set(file("src/main/kotlin"))

            val revision = "${project.version}".let { version ->
                if (version.endsWith("-SNAPSHOT"))
                    "main"
                else
                    "v$version"
            }

            remoteUrl = URI("https://github.com/v47-io/events/blob/$revision/src/main/kotlin")
            remoteLineSuffix = "#L"
        }
    }

    pluginsConfiguration {
        val copyright = "Copyright (c) ${Calendar.getInstance().get(YEAR)} the tmdb-api-client authors"

        html {
            footerMessage = copyright
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates("${project.group}", project.name, "${project.version}")

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
