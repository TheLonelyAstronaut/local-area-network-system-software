package com.astronaut.server.service

import kotlinx.coroutines.flow.Flow

interface FileService {
    suspend fun writeFile(path: String)
    fun readFile(path: String): Flow<ByteArray>
}