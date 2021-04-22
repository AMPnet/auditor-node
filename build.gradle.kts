import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.4.31"
    kotlin("jvm").version(kotlinVersion)

    id("com.google.cloud.tools.jib").version("2.8.0")
    id("org.jlleitschuh.gradle.ktlint").version("10.0.0")
    id("io.gitlab.arturbosch.detekt").version("1.16.0")
    id("org.unbroken-dome.test-sets").version("3.0.1")
    id("com.adarshr.test-logger").version("3.0.0")
    idea
    jacoco
}

group = "com.ampnet"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

testSets {
    create("integTest")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jib {
    val dockerUsername: String = System.getenv("DOCKER_USERNAME") ?: "DOCKER_USERNAME"
    val dockerPassword: String = System.getenv("DOCKER_PASSWORD") ?: "DOCKER_PASSWORD"
    to {
        image = "ampnet/auditor-node:$version"
        auth {
            username = dockerUsername
            password = dockerPassword
        }
        tags = setOf("latest")
    }
    container {
        creationTime = "USE_CURRENT_TIMESTAMP"
    }
}

jacoco.toolVersion = "0.8.6"
tasks.withType<JacocoReport> {
    reports {
        xml.isEnabled = true
        xml.destination = file("$buildDir/reports/jacoco/report.xml")
        csv.isEnabled = false
        html.destination = file("$buildDir/reports/jacoco/html")
    }
    sourceDirectories.setFrom(listOf(file("${project.projectDir}/src/main/kotlin")))
    classDirectories.setFrom(
        fileTree("$buildDir/classes/kotlin/main").apply {
            exclude("**/pojo/**")
        }
    )
    dependsOn(tasks.test)
}
tasks.withType<JacocoCoverageVerification> {
    violationRules {
        rule {
            limit {
                minimum = "0.7".toBigDecimal()
            }
        }
    }
    mustRunAfter(tasks.jacocoTestReport)
}

detekt {
    input = files("src/main/kotlin")
    config = files("detekt-config.yml")
}

task("qualityCheck") {
    dependsOn(tasks.ktlintCheck, tasks.detekt, tasks.jacocoTestReport, tasks.jacocoTestCoverageVerification)
}
