package com.astronaut.server.service.impl

import com.astronaut.common.repository.FileRepository
import com.astronaut.server.service.FileService
import kotlinx.coroutines.flow.Flow

class FileServiceImpl(
    private val fileRepository: FileRepository
): FileService {
    override suspend fun writeFile(path: String, receiveChunk: suspend (ByteArray) -> Int) =
        fileRepository.writeFile(path, receiveChunk)

    override fun readFile(path: String): Flow<ByteArray> = fileRepository.readFile(path)
}