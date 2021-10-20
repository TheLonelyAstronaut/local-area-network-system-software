package com.astronaut.server.service.impl

import com.astronaut.server.repository.FileRepository
import com.astronaut.server.service.FileService

class FileServiceImpl(
    private val fileRepository: FileRepository
): FileService {
}