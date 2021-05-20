package com.ampnet.auditornode.script.api.classes

import arrow.core.right
import assertk.assertThat
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.IpfsTextFile
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import com.ampnet.auditornode.script.api.ExecutionContext
import com.ampnet.auditornode.script.api.model.AuditResult
import com.ampnet.auditornode.script.api.objects.Properties
import com.ampnet.auditornode.service.impl.JavaScriptAuditingService
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

class DirectoryBasedIpfsTest : TestBase() {

    private val httpClient = HttpClient(mock())
    private val environment = Properties(mock())
    private val service = JavaScriptAuditingService(httpClient, environment)
    private val ipfsRepository = mock<IpfsRepository>()

    @Test
    fun `must correctly read IPFS file`() {
        val directoryHash = IpfsHash("directoryHash")
        val ipfs = DirectoryBasedIpfs(directoryHash, ipfsRepository)
        val fileName = "example.js"
        val fileContent = "example file content"

        suppose("IPFS will return some file") {
            given(ipfsRepository.fetchTextFileFromDirectory(directoryHash, fileName))
                .willReturn(IpfsTextFile(fileContent).right())
        }

        verify("IPFS file is correctly returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    assertEquals("Ipfs.getFile()", "example file content", Ipfs.getFile("example.js"));
                    return AuditResult.of(true);
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp.copy(ipfs = ipfs))
            assertThat(result).isRightContaining(AuditResult(true))
        }
    }
}
