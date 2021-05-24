package com.ampnet.auditornode

import io.micronaut.context.env.PropertySource
import io.micronaut.http.client.RxHttpClient
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.AbstractMicronautExtension
import io.micronaut.test.support.server.TestExecutableEmbeddedServer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.fail
import javax.inject.Inject

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class ApiTestWithPropertiesBase(private val propertySourceName: String) : TestBase() {

    @Inject
    // request path must always include serverPath(), e.g. "${serverPath()}/hello"
    protected lateinit var client: RxHttpClient

    @Inject
    private lateinit var server: EmbeddedServer

    @BeforeAll
    fun beforeAll() {
        // This logic is necessary when running tests against the native image - for some reason, Micronaut wants to use
        // only the property source named "test-properties" (AbstractMicronautExtension.TEST_PROPERTY_SOURCE) so this
        // will rename the specified property source to match the name that Micronaut wants.
        if (System.getProperty(TestExecutableEmbeddedServer.PROPERTY) != null) {
            if (server.isRunning) {
                server.stop()
            }

            val scriptPropertySource = server.environment.propertySources.find { it.name == propertySourceName }
                ?: fail("Missing property source with name: $propertySourceName")
            val testPropertySource = server.environment.propertySources.find {
                it.name == AbstractMicronautExtension.TEST_PROPERTY_SOURCE
            } ?: fail("Missing property source with name: ${AbstractMicronautExtension.TEST_PROPERTY_SOURCE}")

            server.environment.removePropertySource(scriptPropertySource)
            server.environment.removePropertySource(testPropertySource)
            server.environment.addPropertySource(
                object : PropertySource {
                    override fun iterator(): MutableIterator<String> = scriptPropertySource.iterator()

                    override fun getName(): String = AbstractMicronautExtension.TEST_PROPERTY_SOURCE

                    override fun get(key: String?): Any? = scriptPropertySource.get(key)
                }
            )

            server.start()
        }
    }

    protected fun serverPath() = "${server.scheme}://${server.host}:${server.port}"

    protected fun webSocketPath() = "ws://${server.host}:${server.port}"
}
