package pt.isel

import java.time.Instant

sealed interface UpdatedMessage {
    data class NewMessage(
        val message: Message,
    ) : UpdatedMessage

    data class KeepAlive(
        val timestamp: Instant,
    ) : UpdatedMessage
}
