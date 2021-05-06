package com.ampnet.auditornode.configuration.properties

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("rpc")
class RpcProperties {
    var url: String = "https://ropsten.infura.io/v3/08664baf7af14eda956db2b71a79f12f"
}
