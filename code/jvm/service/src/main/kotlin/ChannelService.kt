package pt.isel

import jakarta.inject.Named
import java.time.LocalDateTime

@Named
class ChannelService(
    private val trxManager: TransactionManager,
) {
    fun createChannel(
        name: String,
        ownerId: Long,
        isPublic: Boolean,
    ): Either<ChannelError, Channel> {
        if (name.isBlank()) return failure(ChannelError.EmptyChannelName)

        return trxManager.run {
            val owner = repoUsers.findById(ownerId) ?: return@run failure(ChannelError.UserNotFound)

            if (repoChannels.findByName(name) != null) {
                return@run failure(ChannelError.ChannelAlreadyExists)
            }

            val newChannel = repoChannels.create(name, owner, isPublic)
            repoMemberships.addUserToChannel(owner, newChannel, AccessType.READ_WRITE)
            success(newChannel)
        }
    }

    fun getChannelById(channelId: Long): Either<ChannelError, Channel> =
        trxManager.run {
            repoChannels.findById(channelId)?.let { success(it) }
                ?: return@run failure(ChannelError.ChannelNotFound)
        }

    fun getJoinedChannels(
        userId: Long,
        limit: Int = 50,
        offset: Int = 0,
    ): Either<ChannelError, List<Channel>> =
        trxManager.run {
            val user = repoUsers.findById(userId) ?: return@run failure(ChannelError.UserNotFound)
            val channels =
                repoMemberships.findAllChannelsForUser(user, limit, offset).map { it.channel }
            success(channels)
        }

    fun getUsersInChannel(
        channelId: Long,
        limit: Int = 50,
        offset: Int = 0,
    ): Either<ChannelError, List<User>> =
        trxManager.run {
            val channel =
                repoChannels.findById(channelId) ?: return@run failure(ChannelError.ChannelNotFound)
            val users =
                repoMemberships.findAllMembersInChannel(channel, limit, offset).map { it.user }
            success(users)
        }

    fun editChannel(
        ownerId: Long,
        channelId: Long,
        name: String,
        isPublic: Boolean,
    ): Either<ChannelError, Channel> =
        trxManager.run {
            val owner =
                repoUsers.findById(ownerId) ?: return@run failure(ChannelError.UserNotInChannel)
            val channel =
                repoChannels.findById(channelId) ?: return@run failure(ChannelError.ChannelNotFound)
            repoMemberships.findUserInChannel(owner, channel)
                ?: return@run failure(ChannelError.UserNotInChannel)
            if (owner != channel.owner) {
                return@run failure(ChannelError.UserNotOwner)
            }

            val result = channel.copy(name = name, isPublic = isPublic)
            success(result)
        }

    fun getAccessType(
        userId: Long,
        channelId: Long,
    ): Either<MessageError, AccessType> {
        return trxManager.run {
            val user = repoUsers.findById(userId) ?: return@run failure(MessageError.UserNotFound)
            val channel =
                repoChannels.findById(channelId) ?: return@run failure(MessageError.ChannelNotFound)
            val membership =
                repoMemberships.findUserInChannel(user, channel)
                    ?: return@run failure(MessageError.UserNotInChannel)

            success(membership.accessType)
        }
    }

    fun editUser(
        ownerId: Long,
        channelId: Long,
        userId: Long,
        accessType: AccessType,
    ): Either<ChannelError, ChannelMember> =
        trxManager.run {
            val owner = repoUsers.findById(ownerId) ?: return@run failure(ChannelError.UserNotFound)
            val user = repoUsers.findById(userId) ?: return@run failure(ChannelError.UserNotFound)
            val channel =
                repoChannels.findById(channelId) ?: return@run failure(ChannelError.ChannelNotFound)

            val ownerMembership =
                repoMemberships.findUserInChannel(owner, channel)
                    ?: return@run failure(ChannelError.UserNotInChannel)
            val userMembership =
                repoMemberships.findUserInChannel(user, channel)
                    ?: return@run failure(ChannelError.UserNotInChannel)

            if (owner != channel.owner) {
                return@run failure(ChannelError.UserNotOwner)
            }
            if (user == channel.owner) {
                return@run failure(ChannelError.UserIsOwner)
            }
            if (ownerMembership.accessType == AccessType.READ_ONLY) {
                return@run failure(ChannelError.UserNotAuthorized)
            }

            val result = userMembership.copy(accessType = accessType)
            success(result)
        }

    fun searchChannels(
        query: String,
        limit: Int = 50,
        offset: Int = 0,
    ): Either<ChannelError, List<Channel>> =
        trxManager.run {
            val channels =
                if (query.isBlank()) {
                    repoChannels.findAllPublicChannels(limit, offset)
                } else {
                    repoChannels.searchByName(query, limit, offset).filter {
                        it.name.contains(query, ignoreCase = true)
                    }
                }
            success(channels)
        }

    fun joinPublicChannel(
        userId: Long,
        channelId: Long,
    ): Either<ChannelError, String> =
        trxManager.run {
            val user = repoUsers.findById(userId) ?: return@run failure(ChannelError.UserNotFound)
            val channel =
                repoChannels.findById(channelId) ?: return@run failure(ChannelError.ChannelNotFound)

            if (repoMemberships.findUserInChannel(user, channel) != null) {
                return@run failure(ChannelError.UserAlreadyInChannel)
            }

            repoMemberships.addUserToChannel(user, channel, AccessType.READ_WRITE)
            return@run success("Joined public channel '${channel.name}'.")
        }

    fun leaveChannel(
        channelId: Long,
        userId: Long,
    ): Either<ChannelError, String> {
        return trxManager.run {
            val user = repoUsers.findById(userId) ?: return@run failure(ChannelError.UserNotFound)
            val channel =
                repoChannels.findById(channelId) ?: return@run failure(ChannelError.ChannelNotFound)
            val membership =
                repoMemberships.findUserInChannel(user, channel)
                    ?: return@run failure(ChannelError.UserNotInChannel)

            if (channel.owner == user) {
                return@run failure(ChannelError.OwnerCannotLeave)
            }

            repoMemberships.deleteById(membership.id)
            success("Left channel '${channel.name}'")
        }
    }

    fun joinPrivateChannel(
        userId: Long,
        token: String,
    ): Either<ChannelError, String> =
        trxManager.run {
            val user = repoUsers.findById(userId) ?: return@run failure(ChannelError.UserNotFound)
            val invitation =
                repoInvitations.findByToken(token)
                    ?: return@run failure(ChannelError.TokenNotFound)

            if (invitation.expiresAt.isBefore(LocalDateTime.now())) {
                return@run failure(ChannelError.InvitationExpired)
            }
            if (invitation.status != Status.PENDING) {
                return@run failure(ChannelError.InvitationAlreadyUsed)
            }

            val channel =
                repoChannels.findById(invitation.channel.id)
                    ?: return@run failure(ChannelError.ChannelNotFound)

            if (repoMemberships.findUserInChannel(user, channel) != null) {
                return@run failure(ChannelError.UserAlreadyInChannel)
            }

            repoMemberships.addUserToChannel(user, channel, invitation.accessType)
            repoInvitations.save(invitation.copy(status = Status.ACCEPTED))
            success("Joined private channel '${channel.name}'")
        }
}
