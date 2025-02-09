package pt.isel

import jakarta.annotation.PreDestroy
import jakarta.inject.Named
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory

@Named
class MessageEventService(
    private val trxManager: TransactionManager,
) {
    private val listeners = ConcurrentHashMap<Long, MutableSet<UpdatedMessageEmitter>>()
    private val scheduler =
        Executors.newScheduledThreadPool(1).apply {
            scheduleAtFixedRate(::sendKeepAlive, 2, 2, TimeUnit.SECONDS)
        }

    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down message event service")
        scheduler.shutdown()
    }

    fun addEmitter(
        channelId: Long,
        emitter: UpdatedMessageEmitter,
    ) {
        trxManager.run { repoChannels.findById(channelId) }
            ?: throw IllegalArgumentException("Channel with ID $channelId not found.")

        listeners.computeIfAbsent(channelId) { ConcurrentHashMap.newKeySet() }.apply {
            add(emitter)
            logger.debug("Emitter added to channel $channelId. Total emitters: $size")
        }

        emitter.onCompletion { removeEmitter(channelId, emitter) }
        emitter.onError { removeEmitter(channelId, emitter) }
    }

    private fun removeEmitter(
        channelId: Long,
        emitter: UpdatedMessageEmitter,
    ) {
        listeners[channelId]?.run {
            if (remove(emitter)) {
                logger.debug("Emitter removed from channel $channelId. Remaining emitters: $size")
                if (isEmpty()) listeners.remove(channelId)
            }
        }
    }

    private fun sendKeepAlive() {
        val signal = UpdatedMessage.KeepAlive(Instant.now())
        listeners.forEach { (channelId, emitters) ->
            emitters.forEach { emitter ->
                try {
                    emitter.emit(signal)
                } catch (e: Exception) {
                    logger.error("Error sending keep-alive to channel $channelId: ${e.message}", e)
                    removeEmitter(channelId, emitter)
                }
            }
        }
    }

    fun sendMessageToAll(
        channelId: Long,
        signal: UpdatedMessage,
    ) {
        listeners[channelId]?.forEach { emitter ->
            try {
                emitter.emit(signal)
            } catch (e: Exception) {
                logger.error("Error emitting message to channel $channelId: ${e.message}", e)
                removeEmitter(channelId, emitter)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MessageEventService::class.java)
    }
}
