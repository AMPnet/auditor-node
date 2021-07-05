package com.ampnet.auditornode.model.contract

import java.math.BigInteger

@JvmInline
value class Balance(val value: BigInteger)

@JvmInline
value class UsdcPerAudit(val value: BigInteger)

@JvmInline
value class UsdcPerList(val value: BigInteger)
