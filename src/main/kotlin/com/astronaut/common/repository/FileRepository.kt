package com.astronaut.common.repository

import kotlinx.coroutines.flow.Flow

interface FileRepository {
    suspend fun writeFile(path: String, receiveChunk: suspend (ByteArray) -> Int)
    fun readFile(path: String): Flow<ByteArray>
}