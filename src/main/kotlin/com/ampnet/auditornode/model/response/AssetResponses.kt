package com.ampnet.auditornode.model.response

import com.ampnet.auditornode.util.NativeReflection

@NativeReflection
data class AssetResponse(val name: String, val contractAddress: String)

@NativeReflection
data class AssetListResponse(val assets: List<AssetResponse>)
