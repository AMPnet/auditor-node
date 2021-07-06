package com.ampnet.auditornode.persistence.model

@JvmInline
value class IpfsHash(val value: String)

@JvmInline
value class IpfsTextFile(val content: String)

@JvmInline
value class IpfsBinaryFile(val content: ByteArray)

class NamedIpfsFile(
    val content: ByteArray,
    val fileName: String
)
