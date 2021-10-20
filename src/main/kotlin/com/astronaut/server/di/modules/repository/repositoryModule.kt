package com.astronaut.server.di.modules.repository

import com.astronaut.server.repository.FileRepository
import com.astronaut.server.repository.impl.FileRepositoryImpl
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

val repositoryModule = DI.Module("Repository") {
    bind<FileRepository>() with singleton { FileRepositoryImpl() }
}