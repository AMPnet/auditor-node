package com.amptnet.auditornode.documentation.annotation

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class ScriptField(
    val description: String,
    val signature: String = ""
)
