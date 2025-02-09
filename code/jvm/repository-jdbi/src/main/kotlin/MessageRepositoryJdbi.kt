package pt.isel

import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import org.jdbi.v3.core.Handle
import pt.isel.auth.PasswordValidationInfo

class MessageRepositoryJdbi(
    private val handle: Handle,
) : MessageRepository {
    override fun create(
        content: String,
        user: User,
        channel: Channel,
    ): Message {
        val createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val id =
            handle.executeUpdateAndReturnId(
                """
                INSERT INTO dbo.messages (content, user_id, channel_id, created_at)
                VALUES (:content, :user_id, :channel_id, :created_at)
                """,
                mapOf(
                    "content" to content,
                    "user_id" to user.id,
                    "channel_id" to channel.id,
                    "created_at" to createdAt,
                ),
            )
        return Message(id, content, user, channel, createdAt)
    }

    override fun findById(id: Long): Message? =
        handle.executeQueryToSingle(
            """
            SELECT messages.*, users.*, channels.* FROM dbo.messages messages
            JOIN dbo.users users ON messages.user_id = users.id
            JOIN dbo.channels channels ON messages.channel_id = channels.id
            WHERE messages.id = :id
             """,
            mapOf("id" to id),
            ::mapRowToMessage,
        )

    override fun findAllInChannel(
        channel: Channel,
        limit: Int,
        offset: Int,
    ): List<Message> =
        handle.executeQueryToList(
            """
            SELECT messages.*, users.*, channels.* FROM dbo.messages messages
            JOIN dbo.users users ON messages.user_id = users.id
            JOIN dbo.channels channels ON messages.channel_id = channels.id
            WHERE messages.channel_id = :channel_id
            ORDER BY messages.created_at ASC
            OFFSET :offset
            LIMIT :limit
            """,
            mapOf("channel_id" to channel.id, "offset" to offset, "limit" to limit),
            ::mapRowToMessage,
        )

    override fun findAll(): List<Message> =
        handle.executeQueryToList(
            """
            SELECT messages.*, users.*, channels.* FROM dbo.messages messages
            JOIN dbo.users users ON messages.user_id = users.id
            JOIN dbo.channels channels  ON messages.channel_id = channels.id 
            """,
            mapper = ::mapRowToMessage,
        )

    override fun save(entity: Message) {
        handle.executeUpdate(
            """
            UPDATE dbo.messages
            SET content = :content
            WHERE id = :id
            """,
            mapOf("id" to entity.id, "content" to entity.content),
        )
    }

    override fun deleteById(id: Long) {
        handle.executeUpdate("DELETE FROM dbo.messages WHERE id = :id", mapOf("id" to id))
    }

    override fun clear() {
        handle.executeUpdate("DELETE FROM dbo.messages")
    }

    private fun mapRowToMessage(rs: ResultSet): Message {
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

        return Message(
            rs.getLong("id"),
            rs.getString("content"),
            user,
            channel,
            rs.getTimestamp("created_at").toLocalDateTime().truncatedTo(ChronoUnit.MILLIS),
        )
    }
}
