package com.astronaut.common.repository.impl

import com.astronaut.common.repository.FileRepository
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.flow.flow
import java.io.FileInputStream
import java.io.FileOutputStream

val CHUNK_SIZE = 1024

class FileRepositoryImpl: FileRepository {
    override suspend fun writeFile(path: String) {
        TODO("Not yet implemented")
    }

    override fun readFile(path: String) =
        flow<ByteArray> {
            FileInputStream(path).use {
                var actualSize = 0
                var commonSize = 0
                val byteArray = ByteArray(CHUNK_SIZE)

                do {
                    actualSize = it.read(byteArray)

                    if(actualSize != -1) {
                        commonSize += actualSize

                        if(actualSize == CHUNK_SIZE) {
                            emit(byteArray)
                        } else {
                            emit(ByteArray(actualSize) { index ->
                                byteArray[index]
                            })
                        }
                    }

                } while (actualSize != -1)
            }
        }
}