package pt.isel

import jakarta.inject.Named

@Named
class MessageService(
    private val trxManager: TransactionManager,
    private val messageEventService: MessageEventService,
) {
    fun createMessage(
        content: String,
        userId: Long,
        channelId: Long,
    ): Either<MessageError, Message> {
        if (content.isBlank()) return failure(MessageError.EmptyMessage)

        return trxManager.run {
            val user = repoUsers.findById(userId) ?: return@run failure(MessageError.UserNotFound)
            val channel =
                repoChannels.findById(channelId) ?: return@run failure(MessageError.ChannelNotFound)
            val membership =
                repoMemberships.findUserInChannel(user, channel)
                    ?: return@run failure(MessageError.UserNotInChannel)

            if (membership.accessType != AccessType.READ_WRITE) {
                return@run failure(MessageError.UserNotAuthorized)
            }

            val message = repoMessages.create(content, user, channel)
            messageEventService.sendMessageToAll(channelId, UpdatedMessage.NewMessage(message))
            success(message)
        }
    }

    fun getMessagesInChannel(
        userId: Long,
        channelId: Long,
        limit: Int = 50,
        offset: Int = 0,
    ): Either<MessageError, List<Message>> {
        if (limit <= 0) return failure(MessageError.InvalidLimit)
        if (offset < 0) return failure(MessageError.InvalidOffset)

        return trxManager.run {
            val user = repoUsers.findById(userId) ?: return@run failure(MessageError.UserNotFound)
            val channel =
                repoChannels.findById(channelId) ?: return@run failure(MessageError.ChannelNotFound)

            repoMemberships.findUserInChannel(user, channel)
                ?: return@run failure(MessageError.UserNotInChannel)

            val messages = repoMessages.findAllInChannel(channel, limit, offset)

            success(messages)
        }
    }
}
