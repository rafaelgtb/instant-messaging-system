package pt.isel

import jakarta.inject.Named
import java.time.LocalDateTime
import java.util.UUID

@Named
class InvitationService(
    private val trxManager: TransactionManager,
) {
    fun createInvitation(
        creatorId: Long,
        channelId: Long,
        accessType: AccessType,
        expiresAt: LocalDateTime,
    ): Either<InvitationError, Invitation> {
        if (expiresAt.isBefore(LocalDateTime.now())) {
            return failure(InvitationError.InvalidExpirationTime)
        }

        return trxManager.run {
            val creator =
                repoUsers.findById(creatorId) ?: return@run failure(InvitationError.UserNotFound)
            val channel =
                repoChannels.findById(channelId)
                    ?: return@run failure(InvitationError.ChannelNotFound)
            val membership =
                repoMemberships.findUserInChannel(creator, channel)
                    ?: return@run failure(InvitationError.UserNotInChannel)

            if (membership.accessType != AccessType.READ_WRITE) {
                return@run failure(InvitationError.UserNotAuthorized)
            }

            val token = UUID.randomUUID().toString()
            val invitation = repoInvitations.create(token, creator, channel, accessType, expiresAt)
            success(invitation)
        }
    }

    fun getInvitationsForChannel(
        requesterId: Long,
        channelId: Long,
    ): Either<InvitationError, List<Invitation>> =
        trxManager.run {
            val user =
                repoUsers.findById(requesterId) ?: return@run failure(InvitationError.UserNotFound)
            val channel =
                repoChannels.findById(channelId)
                    ?: return@run failure(InvitationError.ChannelNotFound)
            val membership =
                repoMemberships.findUserInChannel(user, channel)
                    ?: return@run failure(InvitationError.UserNotInChannel)

            if (membership.accessType != AccessType.READ_WRITE) {
                return@run failure(InvitationError.UserNotAuthorized)
            }

            val invitations = repoInvitations.findByChannelId(channelId)
            success(invitations)
        }

    fun revokeInvitation(
        userId: Long,
        channelId: Long,
        invitationId: Long,
    ): Either<InvitationError, String> =
        trxManager.run {
            val user =
                repoUsers.findById(userId) ?: return@run failure(InvitationError.UserNotFound)
            val channel =
                repoChannels.findById(channelId)
                    ?: return@run failure(InvitationError.ChannelNotFound)
            val membership =
                repoMemberships.findUserInChannel(user, channel)
                    ?: return@run failure(InvitationError.UserNotInChannel)

            if (user != channel.owner || membership.accessType != AccessType.READ_WRITE) {
                return@run failure(InvitationError.UserNotAuthorized)
            }

            val invitation =
                repoInvitations.findById(invitationId)
                    ?: return@run failure(InvitationError.InvitationNotFound)

            repoInvitations.save(invitation.copy(status = Status.REJECTED))
            success("Invitation revoked.")
        }
}
