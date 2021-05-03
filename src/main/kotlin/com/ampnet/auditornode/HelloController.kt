package com.ampnet.auditornode

import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import javax.inject.Inject

@Controller("/hello") // TODO for example only, remove later
class HelloController @Inject constructor(private val ipfsClientService: IpfsRepository) {

    @Get(produces = [MediaType.TEXT_PLAIN])
    fun index(): String {
        return ipfsClientService.fetchTextFile(IpfsHash("QmSuwCUCZXzPunnrCWL7CnSLixboTa7HftVBjcVgi3TMaK"))
            .toString()
    }
}
