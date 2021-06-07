package com.ampnet.auditornode.persistence.repository

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.persistence.model.ScriptId
import com.ampnet.auditornode.persistence.model.ScriptSource
import com.ampnet.auditornode.persistence.repository.impl.InMemoryScriptRepository
import com.ampnet.auditornode.util.UuidProvider
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import java.util.UUID

class InMemoryScriptRepositoryUnitTest : TestBase() {

    private val uuidProvider = mock<UuidProvider>()
    private val repository = InMemoryScriptRepository(uuidProvider)
    private val storedScriptId = ScriptId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    private val nonExistentScriptId = ScriptId(UUID.fromString("00000000-0000-0000-0000-000000000002"))

    @Test
    fun `must correctly store and load scripts from the repository`() {
        val scriptSource = ScriptSource("test script source")

        suppose("storing a script will return some script ID") {
            given(uuidProvider.getUuid())
                .willReturn(storedScriptId.value)
        }

        verify("storing a script returns correct script ID") {
            val storedScriptId = repository.store(scriptSource)
            assertThat(storedScriptId).isEqualTo(storedScriptId)
        }

        verify("script is correctly loaded for provided script ID") {
            val loadedScript = repository.load(storedScriptId)
            assertThat(loadedScript).isEqualTo(scriptSource)
        }
    }

    @Test
    fun `must return null when there is no stored script for given script ID in the repository`() {
        verify("fetching a script with non-existent ID returns null") {
            val loadedScript = repository.load(nonExistentScriptId)
            assertThat(loadedScript).isNull()
        }
    }
}
