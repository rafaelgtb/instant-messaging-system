package pt.isel

import kotlinx.datetime.Instant
import pt.isel.auth.PasswordValidationInfo
import pt.isel.auth.Token
import pt.isel.auth.TokenValidationInfo

/** Repository interface for managing users, extends the generic Repository */
interface UserRepository : Repository<User> {
    fun create(
        username: String,
        passwordValidationInfo: PasswordValidationInfo,
    ): User

    fun findByUsername(username: String): User?

    fun createToken(
        token: Token,
        maxTokens: Int,
    )

    fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>?

    fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    )

    fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int
}
