package com.amptnet.auditornode.documentation.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class ScriptFunction(
    val description: String,
    val exampleCall: String,
    val signature: String = ""
)
