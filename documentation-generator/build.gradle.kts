import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt")
    idea
}

java.sourceCompatibility = Versions.Compile.sourceCompatibility
java.targetCompatibility = Versions.Compile.targetCompatibility

dependencies {
    kapt("com.google.auto.service:auto-service:${Versions.Dependencies.autoService}")
    kapt(project(":documentation-annotations"))

    compileOnly(project(":documentation-annotations"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.google.auto.service:auto-service:${Versions.Dependencies.autoService}")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = Configurations.Compile.compilerArgs
        jvmTarget = Versions.Compile.jvmTarget
    }
}
