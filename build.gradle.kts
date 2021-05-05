import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.gitlab.arturbosch.detekt.Detekt
import io.micronaut.gradle.MicronautRuntime
import io.micronaut.gradle.MicronautTestRuntime
import io.micronaut.gradle.graalvm.NativeImageTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        maven("https://jitpack.io")
    }

    dependencies {
        classpath("com.github.komputing:kethabi:0.1.9")
    }
}

apply(plugin = "kethabi")

plugins {
    val kotlinVersion = "1.4.0"
    kotlin("jvm").version(kotlinVersion)
    kotlin("kapt").version(kotlinVersion)

    id("org.jetbrains.kotlin.plugin.allopen").version(kotlinVersion)
    id("com.google.cloud.tools.jib").version("2.8.0")
    id("org.jlleitschuh.gradle.ktlint").version("10.0.0")
    id("io.gitlab.arturbosch.detekt").version("1.16.0")
    id("org.unbroken-dome.test-sets").version("3.0.1")
    id("com.adarshr.test-logger").version("3.0.0")
    id("io.micronaut.application").version("1.5.0")
    id("com.github.johnrengelman.shadow").version("6.1.0")
    id("application")
    idea
    jacoco
}

group = "com.ampnet"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://jitpack.io")
}

micronaut {
    version("2.5.0")
    runtime(MicronautRuntime.NETTY)
    testRuntime(MicronautTestRuntime.JUNIT_5)
}

dependencies {
    kapt(platform("io.micronaut:micronaut-bom:${micronaut.version}"))
    kapt("io.micronaut:micronaut-inject-java")
    kapt("io.micronaut:micronaut-validation")
    implementation(platform("io.micronaut:micronaut-bom:${micronaut.version}"))
    implementation("io.micronaut:micronaut-inject")
    implementation("io.micronaut:micronaut-validation")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut:micronaut-http-client")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("org.graalvm.sdk:graal-sdk:21.0.0")
    implementation("org.web3j:core:4.8.4")
    implementation("io.arrow-kt:arrow-core:0.13.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")

    kaptTest(platform("io.micronaut:micronaut-bom:${micronaut.version}"))
    kaptTest("io.micronaut:micronaut-inject-java")
    testImplementation(platform("io.micronaut:micronaut-bom:${micronaut.version}"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

application {
    mainClass.set("com.ampnet.auditornode.AuditorNodeApplication")
}

testSets {
    create("integTest")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xinline-classes")
        jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

allOpen {
    annotation("io.micronaut.aop.Around")
}

kapt {
    arguments {
        arg("micronaut.processing.incremental", true)
        arg("micronaut.processing.annotations", "com.ampnet.auditornode.*")
        arg("micronaut.processing.group", "com.ampnet.auditornode")
        arg("micronaut.processing.module", "auditor-node")
    }
}

tasks.withType<ShadowJar> {
    mergeServiceFiles()
}

tasks.withType<NativeImageTask>() {
    args(
        "-H:ReflectionConfigurationFiles=${project.rootDir}/native-image/reflection-config.json",
        "--initialize-at-build-time",
        "--language:js"
    )
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
    val baseImage = "ghcr.io/graalvm/graalvm-ce"
    val tag = "ol8-java8-21.0.0.2"
    val digest = "sha256:2754d08ca9ca494d6947f214d66e02ab7bd02192ee13ed9e2f5c802d588040e0"
    from {
        image = "$baseImage:$tag@$digest"
    }
    container {
        creationTime = "USE_CURRENT_TIMESTAMP"
        ports = listOf("8080")
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
            exclude("com/ampnet/auditornode/contract/**")
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

tasks.withType<Detekt> {
    exclude("com/ampnet/auditornode/contract/**")
}

ktlint {
    filter {
        exclude("com/ampnet/auditornode/contract/**")
    }
}

task("qualityCheck") {
    dependsOn(tasks.ktlintCheck, tasks.detekt, tasks.jacocoTestReport, tasks.jacocoTestCoverageVerification)
}
