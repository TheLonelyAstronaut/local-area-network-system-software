package com.astronaut.server.controller.impl

import com.astronaut.server.controller.FileController
import com.astronaut.server.service.FileService
import com.astronaut.server.socket.ClientSocket
import com.astronaut.server.utils.Events
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

class FileControllerImpl(
    private val fileService: FileService
): FileController {
    override suspend fun upload(socket: ClientSocket, event: Events.UPLOAD) {
        TODO("Not yet implemented")
    }

    override suspend fun download(socket: ClientSocket, event: Events.DOWNLOAD) {
        fileService.readFile("data/server/${event.filename}")
            .catch {
                socket.writeString(Events.buildErrorString("No such file: ${event.filename}"))
            }
            .collect {
                socket.writeByteArray(it)
            }
    }
}