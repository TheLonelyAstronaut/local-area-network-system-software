package com.astronaut.server.service

import kotlinx.coroutines.flow.Flow

interface FileService {
    suspend fun writeFile(path: String, receiveChunk: suspend (ByteArray) -> Int)
    fun readFile(path: String): Flow<ByteArray>
}