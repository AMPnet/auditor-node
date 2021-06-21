import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    idea
}

java.sourceCompatibility = Versions.Compile.sourceCompatibility
java.targetCompatibility = Versions.Compile.targetCompatibility

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = Configurations.Compile.compilerArgs
        jvmTarget = Versions.Compile.jvmTarget
    }
}
