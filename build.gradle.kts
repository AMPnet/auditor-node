import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.gitlab.arturbosch.detekt.Detekt
import io.micronaut.gradle.MicronautRuntime
import io.micronaut.gradle.MicronautTestRuntime
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension

buildscript {
    repositories {
        maven("https://jitpack.io")
    }

    dependencies {
        classpath("com.github.komputing:kethabi:${Versions.Plugins.kethabi}")
    }
}

apply(plugin = "kethabi")

plugins {
    kotlin("jvm").version(Versions.Compile.kotlin)
    kotlin("kapt").version(Versions.Compile.kotlin)

    id("org.jetbrains.kotlin.plugin.allopen").version(Versions.Plugins.allOpen)
    id("org.jlleitschuh.gradle.ktlint").version(Versions.Plugins.ktlint)
    id("io.gitlab.arturbosch.detekt").version(Versions.Plugins.detekt)
    id("org.unbroken-dome.test-sets").version(Versions.Plugins.testSets)
    id("com.adarshr.test-logger").version(Versions.Plugins.testLogger)
    id("io.micronaut.application").version(Versions.Plugins.micronaut)
    id("com.github.johnrengelman.shadow").version(Versions.Plugins.shadowJar)
    id("application")
    idea
    jacoco
}

extensions.configure(KtlintExtension::class.java) {
    version.set(Versions.Tools.ktlint)
}

group = "com.ampnet"
version = Versions.project
java.sourceCompatibility = Versions.Compile.sourceCompatibility
java.targetCompatibility = Versions.Compile.targetCompatibility

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        maven(url = "https://jitpack.io")
    }
}

micronaut {
    version(Versions.Tools.micronaut)
    runtime(MicronautRuntime.NETTY)
    testRuntime(MicronautTestRuntime.JUNIT_5)
}

testSets {
    Configurations.Tests.testSets.forEach { create(it) }
}

fun DependencyHandler.integTestImplementation(dependencyNotation: Any): Dependency? =
    add("integTestImplementation", dependencyNotation)

fun DependencyHandler.kaptIntegTest(dependencyNotation: Any): Dependency? =
    add("kaptIntegTest", dependencyNotation)

fun DependencyHandler.apiTestImplementation(dependencyNotation: Any): Dependency? =
    add("apiTestImplementation", dependencyNotation)

fun DependencyHandler.kaptApiTest(dependencyNotation: Any): Dependency? =
    add("kaptApiTest", dependencyNotation)

dependencies {
    kapt(platform("io.micronaut:micronaut-bom:${micronaut.version}"))
    kapt("io.micronaut:micronaut-inject-java")
    kapt("io.micronaut:micronaut-validation")
    kapt("io.micronaut.openapi:micronaut-openapi")
    kapt(project(":documentation-generator"))
    implementation(project(":documentation-annotations"))
    implementation(platform("io.micronaut:micronaut-bom:${micronaut.version}"))
    implementation("io.micronaut:micronaut-inject")
    implementation("io.micronaut:micronaut-validation")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.swagger.core.v3:swagger-annotations")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.squareup.okhttp3:okhttp:${Versions.Dependencies.okHttp}")
    implementation("org.graalvm.sdk:graal-sdk:${Versions.Dependencies.graalSdk}")
    implementation("org.web3j:core:${Versions.Dependencies.web3jCore}")
    implementation("io.arrow-kt:arrow-core:${Versions.Dependencies.arrowCore}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.Dependencies.kotlinCoroutines}")
    implementation("io.github.microutils:kotlin-logging-jvm:${Versions.Dependencies.kotlinLogging}")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${Versions.Dependencies.mockitoKotlin}")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:${Versions.Dependencies.assertk}")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    kaptIntegTest(platform("io.micronaut:micronaut-bom:${micronaut.version}"))
    kaptIntegTest("io.micronaut:micronaut-inject-java")
    integTestImplementation(platform("io.micronaut:micronaut-bom:${micronaut.version}"))
    integTestImplementation("io.micronaut.test:micronaut-test-junit5")
    integTestImplementation("io.micronaut.test:micronaut-test-core:${Versions.Dependencies.micronautTestCore}")
    integTestImplementation("org.testcontainers:testcontainers:${Versions.Dependencies.testContainers}")
    integTestImplementation("com.github.tomakehurst:wiremock:${Versions.Dependencies.wireMock}")
    integTestImplementation(sourceSets.test.get().output)

    kaptApiTest(platform("io.micronaut:micronaut-bom:${micronaut.version}"))
    kaptApiTest("io.micronaut:micronaut-inject-java")
    apiTestImplementation(platform("io.micronaut:micronaut-bom:${micronaut.version}"))
    apiTestImplementation("io.micronaut.test:micronaut-test-junit5")
    apiTestImplementation("io.micronaut.test:micronaut-test-core:${Versions.Dependencies.micronautTestCore}")
    apiTestImplementation("com.github.tomakehurst:wiremock:${Versions.Dependencies.wireMock}")
    apiTestImplementation(sourceSets.test.get().output)
}

application {
    mainClass.set("com.ampnet.auditornode.AuditorNodeApplication")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = Configurations.Compile.compilerArgs
        jvmTarget = Versions.Compile.jvmTarget
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
        arg("com.amptnet.auditornode.documentation.output", "$buildDir/documentation")
    }
}

tasks.withType<ShadowJar> {
    mergeServiceFiles()
}

tasks {
    val graalBaseImage = Configurations.Docker.graalBaseImage
    val graalTag = Configurations.Docker.graalTag
    val graalDigest = Configurations.Docker.graalDigest

    dockerfile {
        baseImage("$graalBaseImage:$graalTag@$graalDigest")
        exposePort(8080)
    }

    val dockerUsername: String = System.getenv("DOCKER_USERNAME") ?: "DOCKER_USERNAME"
    val dockerPassword: String = System.getenv("DOCKER_PASSWORD") ?: "DOCKER_PASSWORD"

    dockerBuild {
        val imageName = "ampnet/auditor-node-graal"
        images.set(
            listOf(
                "$imageName:$version",
                "$imageName:latest"
            )
        )
    }

    dockerPush {
        registryCredentials {
            username.set(dockerUsername)
            password.set(dockerPassword)
        }
    }

    nativeImage {
        args(Configurations.NativeImage.args)
        imageName.set("auditor")
    }

    val nativeBaseImage = Configurations.Docker.nativeBaseImage
    val nativeDigest = Configurations.Docker.nativeDigest

    dockerfileNative {
        baseImage("$nativeBaseImage@$nativeDigest")
        exposePort(8080)

        graalImage("$graalBaseImage:$graalTag@$graalDigest")
        args("-H:+StaticExecutableWithDynamicLibC")
    }

    dockerBuildNative {
        val imageName = "ampnet/auditor-node"
        images.set(
            listOf(
                "$imageName:$version",
                "$imageName:latest"
            )
        )
        registryCredentials {
            username.set(dockerUsername)
            password.set(dockerPassword)
        }
    }

    dockerPushNative {
        registryCredentials {
            username.set(dockerUsername)
            password.set(dockerPassword)
        }
    }
}

task("fullTest") {
    val allTests = listOf(tasks.test) + Configurations.Tests.testSets.map { tasks[it] }
    dependsOn(*allTests.toTypedArray())
}

jacoco.toolVersion = Versions.Tools.jacoco
tasks.withType<JacocoReport> {
    val allTestExecFiles = (listOf("test") + Configurations.Tests.testSets)
        .map { "$buildDir/jacoco/$it.exec" }
    executionData(*allTestExecFiles.toTypedArray())

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
    dependsOn(tasks["fullTest"])
}

tasks.withType<JacocoCoverageVerification> {
    val allTestExecFiles = (listOf("test") + Configurations.Tests.testSets)
        .map { "$buildDir/jacoco/$it.exec" }
    executionData(*allTestExecFiles.toTypedArray())

    sourceDirectories.setFrom(listOf(file("${project.projectDir}/src/main/kotlin")))
    classDirectories.setFrom(
        fileTree("$buildDir/classes/kotlin/main").apply {
            exclude("com/ampnet/auditornode/contract/**")
        }
    )

    violationRules {
        rule {
            limit {
                minimum = Configurations.Tests.minimumCoverage
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
