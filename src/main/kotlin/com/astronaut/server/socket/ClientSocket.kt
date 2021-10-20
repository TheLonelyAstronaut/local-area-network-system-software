package com.astronaut.server.socket

interface ClientSocket {
    suspend fun readString(): String?
    suspend fun writeString(data: String): Boolean
    suspend fun writeByteArray(data: ByteArray)
    suspend fun close()
}