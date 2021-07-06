package com.ampnet.auditornode.util

import io.micronaut.core.annotation.Introspected
import io.micronaut.core.annotation.TypeHint
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Uint
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint160
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.DefaultBlockParameterNumber
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.Response
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthBlock
import org.web3j.protocol.core.methods.response.EthBlockNumber
import org.web3j.protocol.core.methods.response.EthCall
import org.web3j.protocol.core.methods.response.EthSyncing
import org.web3j.protocol.core.methods.response.NetVersion
import org.web3j.protocol.deserializer.KeepAsJsonDeserialzier

typealias NativeReflection = Introspected

@TypeHint(
    accessType = [
        TypeHint.AccessType.ALL_DECLARED_CONSTRUCTORS,
        TypeHint.AccessType.ALL_PUBLIC_CONSTRUCTORS,
        TypeHint.AccessType.ALL_DECLARED_METHODS,
        TypeHint.AccessType.ALL_DECLARED_FIELDS,
        TypeHint.AccessType.ALL_PUBLIC_METHODS,
        TypeHint.AccessType.ALL_PUBLIC_FIELDS
    ],
    value = [
        KeepAsJsonDeserialzier::class,
        Request::class,
        Response::class,
        Response.Error::class,
        DefaultBlockParameter::class,
        DefaultBlockParameterNumber::class,
        DefaultBlockParameterName::class,
        EthSyncing::class,
        EthSyncing.Result::class,
        EthSyncing.Syncing::class,
        EthSyncing.ResponseDeserialiser::class,
        EthBlock::class,
        EthBlock.Block::class,
        EthBlock.TransactionResult::class,
        EthBlock.TransactionHash::class,
        EthBlock.TransactionObject::class,
        EthBlock.ResultTransactionDeserialiser::class,
        EthBlock.ResponseDeserialiser::class,
        EthCall::class,
        EthBlockNumber::class,
        NetVersion::class,
        Transaction::class,
        Address::class,
        Uint::class,
        Uint160::class,
        Uint256::class,
        Utf8String::class
    ]
)
@Suppress("unused")
private object Web3jReflections
