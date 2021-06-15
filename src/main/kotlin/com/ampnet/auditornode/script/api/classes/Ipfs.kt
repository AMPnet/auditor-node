package com.ampnet.auditornode.script.api.classes

interface Ipfs {
    fun getFile(fileName: String): String?
    fun linkToFile(fileName: String): String?
}
