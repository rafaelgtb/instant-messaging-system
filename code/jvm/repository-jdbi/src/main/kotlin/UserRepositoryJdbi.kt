package pt.isel

import java.sql.ResultSet
import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.slf4j.LoggerFactory
import pt.isel.auth.PasswordValidationInfo
import pt.isel.auth.Token
import pt.isel.auth.TokenValidationInfo

class UserRepositoryJdbi(
    private val handle: Handle,
) : UserRepository {
    override fun create(
        username: String,
        passwordValidationInfo: PasswordValidationInfo,
    ): User {
        val id =
            handle.executeUpdateAndReturnId(
                """
                INSERT INTO dbo.users (username, password_validation)
                VALUES (:username, :password_validation)
                """,
                mapOf(
                    "username" to username,
                    "password_validation" to passwordValidationInfo.validationInfo,
                ),
            )
        return User(id, username, passwordValidationInfo)
    }

    override fun findById(id: Long): User? =
        handle.executeQueryToSingle(
            "SELECT * FROM dbo.users WHERE id = :id",
            mapOf("id" to id),
            ::mapRowToUser,
        )

    override fun findByUsername(username: String): User? =
        handle.executeQueryToSingle(
            "SELECT * FROM dbo.users WHERE username = :username",
            mapOf("username" to username),
            ::mapRowToUser,
        )

    override fun findAll(): List<User> =
        handle.executeQueryToList("SELECT * FROM dbo.users", mapper = ::mapRowToUser)

    override fun save(entity: User) {
        handle.executeUpdate(
            """
            UPDATE dbo.users
            SET username = :username, password_validation = :password_validation
            WHERE id = :id
            """,
            mapOf(
                "id" to entity.id,
                "username" to entity.username,
                "password_validation" to entity.passwordValidation.validationInfo,
            ),
        )
    }

    override fun deleteById(id: Long) {
        handle.executeUpdate("DELETE FROM dbo.users WHERE id = :id", mapOf("id" to id))
    }

    override fun createToken(
        token: Token,
        maxTokens: Int,
    ) {
        val deletions =
            handle.executeUpdate(
                """
                DELETE FROM dbo.tokens 
                WHERE user_id = :user_id AND token_validation in (
                    SELECT token_validation FROM dbo.tokens 
                    WHERE user_id = :user_id 
                    ORDER BY last_used_at DESC 
                    OFFSET :offset
                )
                """,
                mapOf("user_id" to token.userId, "offset" to maxTokens - 1),
            )

        logger.info("{} tokens deleted when creating new token", deletions)

        handle.executeUpdate(
            """
            INSERT INTO dbo.tokens(user_id, token_validation, created_at, last_used_at) 
            VALUES (:user_id, :token_validation, :created_at, :last_used_at)
            """,
            mapOf(
                "user_id" to token.userId,
                "token_validation" to token.tokenValidationInfo.validationInfo,
                "created_at" to token.createdAt.epochSeconds,
                "last_used_at" to token.lastUsedAt.epochSeconds,
            ),
        )
    }

    override fun getTokenByTokenValidationInfo(
        tokenValidationInfo: TokenValidationInfo,
    ): Pair<User, Token>? =
        handle
            .executeQueryToSingle(
                """
                SELECT users.*, tokens.* FROM dbo.users users
                INNER JOIN dbo.tokens tokens ON users.id = tokens.user_id
                WHERE tokens.token_validation = :validation_information
                """,
                mapOf("validation_information" to tokenValidationInfo.validationInfo),
                ::mapRowToUserAndToken,
            )?.userAndToken

    override fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    ) {
        handle.executeUpdate(
            """
            UPDATE dbo.tokens
            SET last_used_at = :last_used_at
            WHERE token_validation = :validation_information
            """,
            mapOf(
                "last_used_at" to now.epochSeconds,
                "validation_information" to token.tokenValidationInfo.validationInfo,
            ),
        )
    }

    override fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int =
        handle.executeUpdate(
            "DELETE FROM dbo.tokens WHERE token_validation = :validation_information",
            mapOf("validation_information" to tokenValidationInfo.validationInfo),
        )

    override fun clear() {
        handle.executeUpdate("DELETE FROM dbo.users")
        handle.executeUpdate("DELETE FROM dbo.tokens")
    }

    private fun mapRowToUser(rs: ResultSet): User =
        User(
            rs.getLong("id"),
            rs.getString("username"),
            PasswordValidationInfo(rs.getString("password_validation")),
        )

    private fun mapRowToUserAndToken(rs: ResultSet): UserAndTokenModel =
        UserAndTokenModel(
            rs.getLong("id"),
            rs.getString("username"),
            PasswordValidationInfo(rs.getString("password_validation")),
            TokenValidationInfo(rs.getString("token_validation")),
            rs.getLong("created_at"),
            rs.getLong("last_used_at"),
        )

    private data class UserAndTokenModel(
        val id: Long,
        val username: String,
        val passwordValidation: PasswordValidationInfo,
        val tokenValidation: TokenValidationInfo,
        val createdAt: Long,
        val lastUsedAt: Long,
    ) {
        val userAndToken: Pair<User, Token>
            get() =
                Pair(
                    User(id, username, passwordValidation),
                    Token(
                        tokenValidation,
                        id,
                        Instant.fromEpochSeconds(createdAt),
                        Instant.fromEpochSeconds(lastUsedAt),
                    ),
                )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserRepositoryJdbi::class.java)
    }
}
