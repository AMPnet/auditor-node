package com.ampnet.auditornode.util

import java.util.UUID

interface UuidProvider {
    fun getUuid(): UUID
}
