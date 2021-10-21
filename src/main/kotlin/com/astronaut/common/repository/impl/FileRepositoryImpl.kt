package com.astronaut.common.repository.impl

import com.astronaut.common.repository.FileRepository
import kotlinx.coroutines.flow.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

const val CHUNK_SIZE = 8192

class FileRepositoryImpl: FileRepository {
    override suspend fun writeFile(path: String, receiveChunk: suspend (ByteArray) -> Int) {
        val outputStream = FileOutputStream(path)

        var actualSize: Int
        var commonSize = 0

        val start = Date().time

        do {
            val byteArray = ByteArray(CHUNK_SIZE)

            actualSize = receiveChunk(byteArray)
            //println(actualSize)

            if(actualSize != -1) {
                commonSize += actualSize

                outputStream.write(byteArray, 0, actualSize)

                if(actualSize != CHUNK_SIZE) {
                    actualSize = -1
                }
            }
        } while (actualSize != -1)

        println("Received: ${Date().time - start} (${commonSize}B)")

        outputStream.close()
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