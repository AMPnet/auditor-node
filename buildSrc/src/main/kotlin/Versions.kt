import org.gradle.api.JavaVersion

object Versions {

    const val project = "0.0.1"

    object Compile {
        const val kotlin = "1.5.0"
        val sourceCompatibility = JavaVersion.VERSION_1_8
        val targetCompatibility = JavaVersion.VERSION_1_8
        val jvmTarget = targetCompatibility.name.removePrefix("VERSION_").replace('_', '.')
    }

    object Plugins {
        const val kethabi = "0.2.0"
        const val allOpen = Compile.kotlin
        const val ktlint = "10.0.0"
        const val detekt = "1.17.1"
        const val testSets = "4.0.0"
        const val testLogger = "3.0.0"
        const val micronaut = "1.5.0"
        const val shadowJar = "6.1.0"
    }

    object Tools {
        const val ktlint = "0.41.0"
        const val micronaut = "2.5.4"
        const val jacoco = "0.8.7"
    }

    object Dependencies {
        const val okHttp = "4.9.1"
        const val graalSdk = "21.0.0.2"
        const val web3jCore = "5.0.0"
        const val arrowCore = "0.13.2"
        const val kotlinCoroutines = Compile.kotlin
        const val kotlinLogging = "2.0.6"
        const val mockitoKotlin = "3.2.0"
        const val assertk = "0.24"
        const val micronautTestCore = "2.3.6"
        const val wireMock = "2.27.2"
        const val testContainers = "1.15.3"
    }
}
