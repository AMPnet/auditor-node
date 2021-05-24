package com.ampnet.auditornode.script.api.classes

import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import org.graalvm.polyglot.HostAccess.Export

class DirectoryBasedIpfs(private val directoryHash: IpfsHash, private val ipfsRepository: IpfsRepository) : Ipfs {

    @Export
    override fun getFile(fileName: String): String? {
        return ipfsRepository.fetchTextFileFromDirectory(directoryHash, fileName)
            .fold(
                ifLeft = { null },
                ifRight = { it.content }
            )
    }
}
