package com.ampnet.auditornode.model.response

import com.ampnet.auditornode.util.NativeReflection
import io.swagger.v3.oas.annotations.media.Schema

@Schema
@NativeReflection
data class AssetResponse(val name: String, val contractAddress: String)

@Schema
@NativeReflection
data class AssetListResponse(val assets: List<AssetResponse>)
