package com.astronaut.common.repository

import kotlinx.coroutines.flow.Flow

interface FileRepository {
    suspend fun writeFile(path: String)
    fun readFile(path: String): Flow<ByteArray>
}