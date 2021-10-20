package com.astronaut.server.utils

sealed class Events {
    data class ECHO(val data: String): Events()
    data class TIME(val data: String): Events()
    data class CLOSE(val data: String): Events()
    data class DOWNLOAD(val data: String): Events()
    data class UPLOAD(val data: String): Events()
    data class UNKNOWN(val data: String): Events()

    companion object {
        fun parseFromClientString(data: String): Events =
            when {
                data.startsWith("ECHO ") -> {
                    Events.ECHO(data)
                }
                data.startsWith("TIME") -> {
                    Events.TIME(data)
                }
                data.startsWith("CLOSE") -> {
                    Events.CLOSE(data)
                }
                data.startsWith("DOWNLOAD ") -> {
                    Events.DOWNLOAD(data)
                }
                data.startsWith("UPLOAD ") -> {
                    Events.UPLOAD(data)
                }
                else -> {
                    Events.UNKNOWN(data)
                }
            }

    }
}