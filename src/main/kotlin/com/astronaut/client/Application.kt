package com.astronaut.client

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress

fun main() {
    runBlocking {
        val address = InetSocketAddress("0.0.0.0", 2323);
        val local = InetSocketAddress("0.0.0.0", 2525);

        val socket =
            aSocket(ActorSelectorManager(coroutineContext))
                .tcp()
                .connect(
                    remoteAddress = address,
                    // localAddress = local,
                    configure = {
                        this.reusePort = true
                    }
                )


        val channel = socket.openReadChannel()
        val channel2 = socket.openWriteChannel(autoFlush = true)

        while (true) {
            val line = readLine() ?: ""

            //socket.send(Datagram(ByteReadPacket(line.encodeToByteArray()), address))
            //print(socket.incoming.receive().packet.readText())
            channel2.writeAvailable("$line\r\n".encodeToByteArray())

            //println(line)

            println(channel.readUTF8Line(Int.MAX_VALUE))
            println(channel.readUTF8Line(Int.MAX_VALUE))
            println(channel.readUTF8Line(Int.MAX_VALUE))
            println(channel.readUTF8Line(Int.MAX_VALUE))
            println(channel.readUTF8Line(Int.MAX_VALUE))
        }

        /*while (true) {
            val line = readLine() ?: ""

            socket.send(Datagram(ByteReadPacket(line.encodeToByteArray()), address))

            println(socket.incoming.receive().packet.readText())
            println(socket.incoming.receive().packet.readText())
            println(socket.incoming.receive().packet.readText())
            println(socket.incoming.receive().packet.readText())
            println(socket.incoming.receive().packet.readText())
        }*/
    }
}