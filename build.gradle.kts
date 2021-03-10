import name.remal.gradle_plugins.dsl.extensions.applyPlugin
import name.remal.gradle_plugins.dsl.extensions.testRuntime
import name.remal.gradle_plugins.plugins.publish.ossrh.RepositoryHandlerOssrhExtension
import java.util.*

plugins {
    kotlin("jvm") version "1.4.31"

    id("io.gitlab.arturbosch.detekt") version "1.16.0-RC3"
    id("jacoco")
    id("de.jansauer.printcoverage") version "2.0.0"

    id("net.researchgate.release") version "2.8.1"

    id("org.jetbrains.dokka") version "1.4.20"

    id("com.github.hierynomus.license") version "0.15.0"
    id("com.github.jk1.dependency-license-report") version "1.16"

    id("maven-publish")
    id("name.remal.maven-publish-ossrh") version "1.2.2" apply false
}

sourceSets.main {
    withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
        kotlin.setSrcDirs(listOf("src", "src-gen", "src-java"))
    }

    java.setSrcDirs(listOf("src-gen", "src-java"))
    resources.setSrcDirs(listOf("resources", "resources-gen"))
}

sourceSets.test {
    withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
        kotlin.setSrcDirs(listOf("test", "test-java"))
    }

    java.setSrcDirs(listOf("test-java"))
    resources.setSrcDirs(listOf("test-resources"))
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

tasks.compileKotlin.configure {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions"
        )

        jvmTarget = "1.8"
    }
}

tasks.compileTestKotlin.configure {
    kotlinOptions.jvmTarget = "1.8"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
    implementation("org.slf4j:slf4j-api:1.7.30")

    val junitVersion = "5.7.1"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")

    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testRuntime("ch.qos.logback:logback-classic:1.2.3")
}

detekt {
    input = files("src")
    config = files("detekt.yml")

    reports {
        xml {
            enabled = true
            destination = file("$buildDir/reports/detekt.xml")
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
    toolVersion = "0.8.6"
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
    tagTemplate = "v\$version"
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
