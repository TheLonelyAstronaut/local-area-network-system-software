package com.astronaut.server.utils

sealed class Events {
    data class ECHO(val string: String): Events()
    data class TIME(val data: String): Events()
    data class CLOSE(val data: String): Events()
    data class DOWNLOAD(val filename: String): Events()
    data class UPLOAD(val filename: String): Events()
    data class UNKNOWN(val data: String): Events()

    companion object {
        fun parseFromClientString(data: String): Events =
            when {
                data.startsWith("ECHO ") -> {
                    Events.ECHO(data.substring(5, data.length))
                }
                data.startsWith("TIME") -> {
                    Events.TIME(data)
                }
                data.startsWith("CLOSE") -> {
                    Events.CLOSE(data)
                }
                data.startsWith("DOWNLOAD ") -> {
                    Events.DOWNLOAD(data.substring(9, data.length))
                }
                data.startsWith("UPLOAD ") -> {
                    Events.UPLOAD(data.substring(7, data.length))
                }
                else -> {
                    Events.UNKNOWN(data)
                }
            }

        fun buildErrorString(message: String) = "ERROR: $message"
    }
}