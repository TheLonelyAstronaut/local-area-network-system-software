package com.astronaut.server.controller.impl

import com.astronaut.server.controller.FileController
import com.astronaut.server.service.FileService
import com.astronaut.server.socket.ClientSocket
import com.astronaut.common.utils.Events
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion

class FileControllerImpl(
    private val fileService: FileService
): FileController {
    override suspend fun upload(socket: ClientSocket, event: Events.UPLOAD) {
        val path = "data/server/${event.filename}"

        val size = fileService.getFileSize(path);

        socket.writeString(Events.OK(size).toString())

        when(val command = Events.parseFromClientString(socket.readString() ?: "")) {
            is Events.START -> {
                println("Starting transmission...")
            }
            is Events.END -> {
                println("File already uploaded")
                return
            }
            else -> {
                println("Unexpected command: $command")
            }
        }

        fileService.writeFile(path, event.size, size) {
            socket.readByteArray(it) ?: -1
        }

        socket.forceApproval()

        compare(event.filename)
    }

    override suspend fun download(socket: ClientSocket, event: Events.DOWNLOAD) {
        val path = "data/server/${event.filename}"

        val size = fileService.getFileSize(path);

        socket.writeString(Events.OK(size).toString())

        if(event.size == size) {
            println("File was already downloaded!")
            return
        }

        var accumulator = 0;

        fileService.readFile(path, event.size)
            .catch {
                socket.writeString(Events.ERROR("No such file: ${event.filename}").toString())
            }
            .onCompletion {
                //socket.close() // <-- Uncomment this to test connection interruption
                //println(accumulator)
                compare(event.filename)
            }
            .collect {
                //accumulator += it.size // <-- Uncomment this to test connection interruption

                if(accumulator <= 2000000) {
                    socket.writeByteArray(it.data)
                }
            }
    }

    private suspend fun compare(path: String) {
        val path1 = "data/server/${path}"
        val path2 = "data/client/${path}"

        val size1 = fileService.getFileSize(path1)
        val size2 = fileService.getFileSize(path2)

        val firstFile: MutableList<ByteArray> = mutableListOf()
        val secondFile: MutableList<ByteArray> = mutableListOf()

        fileService.readFile(path1, 0)
            .collect {
                firstFile.add(it.data.clone())
            }

        fileService.readFile(path2, 0)
            .collect {
                secondFile.add(it.data.clone())
            }

        println("TESTING")
        println("Size equals: ${size1 == size2}")
        println("List sizes equals: ${firstFile.size} ${secondFile.size}")
        println("Cluster compare:")

        for(i in firstFile.indices) {
            val firstCluster = firstFile[i]
            val secondCluster = secondFile[i]
            var isEqual = true

            for(j in firstCluster.indices) {
                if(firstCluster[j] != secondCluster[j]) {
                    isEqual = false
                    break
                }
            }

            if(!isEqual) {
                println("Difference in cluster N${i}")
            }
        }

        println("Done!")
    }
}