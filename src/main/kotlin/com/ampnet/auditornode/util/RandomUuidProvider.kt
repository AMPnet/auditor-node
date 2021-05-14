package com.ampnet.auditornode.util

import java.util.UUID
import javax.inject.Singleton

@Singleton
class RandomUuidProvider : UuidProvider {
    override fun getUuid(): UUID = UUID.randomUUID()
}
