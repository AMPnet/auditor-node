import java.math.BigDecimal

object Configurations {

    object Compile {
        val compilerArgs = listOf("-Xjsr305=strict")
    }

    object Docker {
        const val graalBaseImage = "ghcr.io/graalvm/graalvm-ce"
        const val graalTag = "ol8-java8-${Versions.Dependencies.graalSdk}"
        const val graalDigest = "sha256:2754d08ca9ca494d6947f214d66e02ab7bd02192ee13ed9e2f5c802d588040e0"
        const val nativeBaseImage = "gcr.io/distroless/cc-debian10"
        const val nativeDigest = "sha256:4cad7484b00d98ecb300916b1ab71d6c71babd6860c6c5dd6313be41a8c55adb"
    }

    object NativeImage {
        val args = listOf(
            "--initialize-at-build-time",
            "--language:js"
        )
    }

    object Tests {
        val testSets = listOf("integTest", "apiTest")
        val minimumCoverage = BigDecimal("0.90")
    }
}
