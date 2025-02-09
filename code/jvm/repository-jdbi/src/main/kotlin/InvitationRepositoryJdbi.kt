package pt.isel

import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import org.jdbi.v3.core.Handle
import pt.isel.auth.PasswordValidationInfo

class InvitationRepositoryJdbi(
    private val handle: Handle,
) : InvitationRepository {
    override fun create(
        token: String,
        createdBy: User,
        channel: Channel,
        accessType: AccessType,
        expiresAt: LocalDateTime,
    ): Invitation {
        val expiration = expiresAt.truncatedTo(ChronoUnit.MILLIS)
        val id =
            handle.executeUpdateAndReturnId(
                """
                INSERT INTO dbo.invitations (token, created_by, channel_id, access_type, expires_at) 
                VALUES (:token, :created_by, :channel_id, :access_type, :expires_at)
                """,
                mapOf(
                    "token" to token,
                    "created_by" to createdBy.id,
                    "channel_id" to channel.id,
                    "access_type" to accessType.toDatabaseValue(),
                    "expires_at" to expiration,
                ),
            )
        return Invitation(id, token, createdBy, channel, accessType, expiration)
    }

    override fun findById(id: Long): Invitation? =
        handle.executeQueryToSingle(
            """
            SELECT invitations.*, users.*, channels.* 
            FROM dbo.invitations invitations
            JOIN dbo.users users ON invitations.created_by = users.id
            JOIN dbo.channels channels ON invitations.channel_id = channels.id
            WHERE invitations.id = :id
            """,
            mapOf("id" to id),
            ::mapRowToInvitation,
        )

    override fun findByToken(token: String): Invitation? =
        handle.executeQueryToSingle(
            """
            SELECT invitations.*, users.*, channels.* 
            FROM dbo.invitations invitations
            JOIN dbo.users users ON invitations.created_by = users.id
            JOIN dbo.channels channels ON invitations.channel_id = channels.id
            WHERE invitations.token = :token
            """,
            mapOf("token" to token),
            ::mapRowToInvitation,
        )

    override fun findByChannelId(channelId: Long): List<Invitation> =
        handle.executeQueryToList(
            """
            SELECT invitations.*, users.*, channels.*
            FROM dbo.invitations invitations
            JOIN dbo.users users ON invitations.created_by = users.id
            JOIN dbo.channels channels ON invitations.channel_id = channels.id
            WHERE invitations.channel_id = :channelId
            """,
            mapOf("channelId" to channelId),
            ::mapRowToInvitation,
        )

    override fun findAll(): List<Invitation> =
        handle.executeQueryToList(
            """
            SELECT invitations.*, users.*, channels.* 
            FROM dbo.invitations invitations
            JOIN dbo.users users ON invitations.created_by = users.id
            JOIN dbo.channels channels ON invitations.channel_id = channels.id
            """,
            mapper = ::mapRowToInvitation,
        )

    override fun save(entity: Invitation) {
        handle.executeUpdate(
            """
            UPDATE dbo.invitations
            SET status = :status
            WHERE id = :id
            """,
            mapOf(
                "id" to entity.id,
                "status" to entity.status.toDatabaseValue(),
            ),
        )
    }

    override fun deleteById(id: Long) {
        handle.executeUpdate("DELETE FROM dbo.invitations WHERE id = :id", mapOf("id" to id))
    }

    override fun clear() {
        handle.executeUpdate("DELETE FROM dbo.invitations")
    }

    private fun mapRowToInvitation(rs: ResultSet): Invitation {
        val user =
            User(
                rs.getLong("created_by"),
                rs.getString("username"),
                PasswordValidationInfo(rs.getString("password_validation")),
            )

        val channel =
            Channel(
                rs.getLong("channel_id"),
                rs.getString("name"),
                user,
                rs.getBoolean("is_public"),
            )

        return Invitation(
            rs.getLong("id"),
            rs.getString("token"),
            user,
            channel,
            AccessType.fromDatabase(rs.getString("access_type")),
            rs.getTimestamp("expires_at").toLocalDateTime().truncatedTo(ChronoUnit.MILLIS),
            Status.fromDatabase(rs.getString("status")),
        )
    }
}
