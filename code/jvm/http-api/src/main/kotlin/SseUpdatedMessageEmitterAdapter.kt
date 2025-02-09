package pt.isel

import java.io.IOException
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

class SseUpdatedMessageEmitterAdapter(
    private val sseEmitter: SseEmitter,
) : UpdatedMessageEmitter {
    override fun emit(signal: UpdatedMessage) {
        val sseEvent =
            when (signal) {
                is UpdatedMessage.NewMessage ->
                    SseEmitter
                        .event()
                        .id(signal.message.id.toString())
                        .name("new-message")
                        .data(signal.message)

                is UpdatedMessage.KeepAlive ->
                    SseEmitter
                        .event()
                        .comment("keep-alive: ${signal.timestamp.epochSecond}")
            }
        try {
            sseEmitter.send(sseEvent)
        } catch (e: IOException) {
            sseEmitter.completeWithError(e)
            throw e
        }
    }

    override fun onCompletion(callback: () -> Unit) {
        sseEmitter.onCompletion(callback)
    }

    override fun onError(callback: (Throwable) -> Unit) {
        sseEmitter.onError(callback)
    }
}
