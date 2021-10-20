package com.astronaut.server.controller.impl

import com.astronaut.server.controller.FileController
import com.astronaut.server.service.FileService
import com.astronaut.server.socket.ClientSocket
import com.astronaut.server.utils.Events

class FileControllerImpl(
    private val fireService: FileService
): FileController {
    override fun upload(socket: ClientSocket, event: Events) {
        TODO("Not yet implemented")
    }

    override fun download(socket: ClientSocket, event: Events) {
        TODO("Not yet implemented")
    }
}