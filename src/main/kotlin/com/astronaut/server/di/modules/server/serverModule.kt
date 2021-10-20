package com.astronaut.server.di.modules.server

import com.astronaut.server.server.Server
import com.astronaut.server.server.impl.ServerImpl
import com.astronaut.server.server.ServerSocketWrapper
import com.astronaut.server.server.impl.ServerSocketWrapperImpl
import org.kodein.di.*

val serverModule = DI.Module("Server") {
    bind<ServerSocketWrapper>() with singleton { ServerSocketWrapperImpl(instance()) }
    bind<Server>() with singleton { ServerImpl(instance(), instance(), instance(), instance()) }
}