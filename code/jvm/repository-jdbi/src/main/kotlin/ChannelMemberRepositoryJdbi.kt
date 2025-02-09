package pt.isel

import java.sql.ResultSet
import org.jdbi.v3.core.Handle
import pt.isel.auth.PasswordValidationInfo

class ChannelMemberRepositoryJdbi(
    private val handle: Handle,
) : ChannelMemberRepository {
    override fun addUserToChannel(
        user: User,
        channel: Channel,
        accessType: AccessType,
    ): ChannelMember {
        val id =
            handle.executeUpdateAndReturnId(
                """
                INSERT INTO dbo.channel_members (user_id, channel_id, access_type)
                VALUES (:user_id, :channel_id, :access_type)
                """,
                mapOf(
                    "user_id" to user.id,
                    "channel_id" to channel.id,
                    "access_type" to accessType.toDatabaseValue(),
                ),
            )
        return ChannelMember(id, user, channel, accessType)
    }

    override fun findById(id: Long): ChannelMember? =
        handle.executeQueryToSingle(
            """
            SELECT channel_members.*, users.*, channels.* 
            FROM dbo.channel_members channel_members
            JOIN dbo.users users ON channel_members.user_id = users.id
            JOIN dbo.channels ON channel_members.channel_id = channels.id
            WHERE channel_members.id = :id
            """,
            mapOf("id" to id),
            ::mapRowToChannelMember,
        )

    override fun findUserInChannel(
        user: User,
        channel: Channel,
    ): ChannelMember? =
        handle.executeQueryToSingle(
            """
            SELECT channel_members.*, users.*, channels.* 
            FROM dbo.channel_members channel_members
            JOIN dbo.users users ON channel_members.user_id = users.id
            JOIN dbo.channels ON channel_members.channel_id = channels.id
            WHERE channel_members.channel_id = :channel_id 
            AND channel_members.user_id = :user_id
            """,
            mapOf("channel_id" to channel.id, "user_id" to user.id),
            ::mapRowToChannelMember,
        )

    override fun findAllChannelsForUser(
        user: User,
        limit: Int,
        offset: Int,
    ): List<ChannelMember> =
        handle.executeQueryToList(
            """
            SELECT channel_members.*, users.*, channels.* 
            FROM dbo.channel_members channel_members
            JOIN dbo.users users ON channel_members.user_id = users.id
            JOIN dbo.channels ON channel_members.channel_id = channels.id
            WHERE channel_members.user_id = :user_id
            OFFSET :offset
            LIMIT :limit
            """,
            mapOf("user_id" to user.id, "offset" to offset, "limit" to limit),
            ::mapRowToChannelMember,
        )

    override fun findAllMembersInChannel(
        channel: Channel,
        limit: Int,
        offset: Int,
    ): List<ChannelMember> =
        handle.executeQueryToList(
            """
            SELECT channel_members.*, users.*, channels.* 
            FROM dbo.channel_members channel_members
            JOIN dbo.users users ON channel_members.user_id = users.id
            JOIN dbo.channels ON channel_members.channel_id = channels.id
            WHERE channel_members.channel_id = :channel_id
            OFFSET :offset
            LIMIT :limit
            """,
            mapOf("channel_id" to channel.id, "offset" to offset, "limit" to limit),
            ::mapRowToChannelMember,
        )

    override fun findAll(): List<ChannelMember> =
        handle.executeQueryToList(
            """
            SELECT channel_members.*, users.*, channels.* 
            FROM dbo.channel_members channel_members
            JOIN dbo.users users ON channel_members.user_id = users.id
            JOIN dbo.channels ON channel_members.channel_id = channels.id
            """,
            mapper = ::mapRowToChannelMember,
        )

    override fun save(entity: ChannelMember) {
        handle.executeUpdate(
            """
            UPDATE dbo.channel_members
            SET access_type = :accessType
            WHERE id = :id
            """,
            mapOf("id" to entity.id, "accessType" to entity.accessType.toDatabaseValue()),
        )
    }

    override fun removeUserFromChannel(
        user: User,
        channel: Channel,
    ) {
        handle.executeUpdate(
            """
            DELETE FROM dbo.channel_members
            WHERE user_id = :user_id AND channel_id = :channel_id
            """,
            mapOf("user_id" to user.id, "channel_id" to channel.id),
        )
    }

    override fun deleteById(id: Long) {
        handle.executeUpdate("DELETE FROM dbo.channel_members WHERE id = :id", mapOf("id" to id))
    }

    override fun clear() {
        handle.executeUpdate("DELETE FROM dbo.channel_members")
    }

    private fun mapRowToChannelMember(rs: ResultSet): ChannelMember {
        val user =
            User(
                rs.getLong("user_id"),
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

        return ChannelMember(
            rs.getLong("id"),
            user,
            channel,
            AccessType.fromDatabase(rs.getString("access_type")),
        )
    }
}
