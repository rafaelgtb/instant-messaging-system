package pt.isel

import java.sql.ResultSet
import org.jdbi.v3.core.Handle
import pt.isel.auth.PasswordValidationInfo

class ChannelRepositoryJdbi(
    private val handle: Handle,
) : ChannelRepository {
    override fun create(
        name: String,
        owner: User,
        isPublic: Boolean,
    ): Channel {
        val id =
            handle.executeUpdateAndReturnId(
                """
                INSERT INTO dbo.channels (name, owner_id, is_public)
                VALUES (:name, :owner_id, :is_public)
                """,
                mapOf("name" to name, "owner_id" to owner.id, "is_public" to isPublic),
            )
        return Channel(id, name, owner, isPublic)
    }

    override fun findById(id: Long): Channel? =
        handle.executeQueryToSingle(
            """
            SELECT channels.*, users.* FROM dbo.channels channels
            JOIN dbo.users users ON channels.owner_id = users.id
            WHERE channels.id = :id
            """,
            mapOf("id" to id),
            ::mapRowToChannel,
        )

    override fun findByName(name: String): Channel? =
        handle.executeQueryToSingle(
            """
            SELECT channels.*, users.* FROM dbo.channels channels
            JOIN dbo.users users ON channels.owner_id = users.id
            WHERE channels.name = :name
            """,
            mapOf("name" to name),
            ::mapRowToChannel,
        )

    override fun findAllByOwner(owner: User): List<Channel> =
        handle.executeQueryToList(
            """
            SELECT channels.*, users.* from dbo.channels channels
            JOIN dbo.users users ON channels.owner_id = users.id
            WHERE owner_id = :owner_id
            """,
            mapOf("owner_id" to owner.id),
            ::mapRowToChannel,
        )

    override fun findAllPublicChannels(
        limit: Int,
        offset: Int,
    ): List<Channel> =
        handle.executeQueryToList(
            """
            SELECT channels.*, users.* FROM dbo.channels channels
            JOIN dbo.users users ON channels.owner_id = users.id
            WHERE channels.is_public = TRUE
            OFFSET :offset
            LIMIT :limit
            """,
            mapOf("offset" to offset, "limit" to limit),
            ::mapRowToChannel,
        )

    override fun findAll(): List<Channel> =
        handle.executeQueryToList(
            """
            SELECT channels.*, users.* from dbo.channels channels
            JOIN dbo.users users ON channels.owner_id = users.id
            """,
            mapper = ::mapRowToChannel,
        )

    override fun searchByName(
        query: String,
        limit: Int,
        offset: Int,
    ): List<Channel> =
        handle.executeQueryToList(
            """
            SELECT channels.*, users.* FROM dbo.channels channels
            JOIN dbo.users users ON channels.owner_id = users.id
            WHERE channels.name ILIKE '%' || :query || '%' AND channels.is_public = TRUE
            OFFSET :offset
            LIMIT :limit
            """,
            mapOf("query" to query, "offset" to offset, "limit" to limit),
            ::mapRowToChannel,
        )

    override fun save(entity: Channel) {
        handle.executeUpdate(
            """
            UPDATE dbo.channels
            SET name = :name, is_public = :is_public
            WHERE id = :id
            """,
            mapOf(
                "id" to entity.id,
                "name" to entity.name,
                "is_public" to entity.isPublic,
            ),
        )
    }

    override fun deleteById(id: Long) {
        handle.executeUpdate("DELETE FROM dbo.channels WHERE id = :id", mapOf("id" to id))
    }

    override fun clear() {
        handle.executeUpdate("DELETE FROM dbo.channels")
    }

    private fun mapRowToChannel(rs: ResultSet): Channel {
        val owner =
            User(
                rs.getLong("owner_id"),
                rs.getString("username"),
                PasswordValidationInfo(rs.getString("password_validation")),
            )

        return Channel(
            rs.getLong("id"),
            rs.getString("name"),
            owner,
            rs.getBoolean("is_public"),
        )
    }
}
