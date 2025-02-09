package pt.isel.mem

import jakarta.inject.Named
import kotlinx.datetime.Instant
import pt.isel.User
import pt.isel.UserRepository
import pt.isel.auth.PasswordValidationInfo
import pt.isel.auth.Token
import pt.isel.auth.TokenValidationInfo

/**
 * Naif in memory repository non thread-safe and basic sequential id. Useful for unit tests purpose.
 */
@Named
class UserRepositoryInMem : UserRepository {
    private val users = mutableListOf<User>()
    private val tokens = mutableListOf<Token>()

    override fun create(
        username: String,
        passwordValidationInfo: PasswordValidationInfo,
    ): User = User(users.size.toLong() + 1, username, passwordValidationInfo).also { users.add(it) }

    override fun findById(id: Long): User? = users.firstOrNull { it.id == id }

    override fun findByUsername(username: String): User? =
        users.firstOrNull { it.username == username }

    override fun findAll(): List<User> = users.toList()

    override fun save(entity: User) {
        users.removeIf { it.id == entity.id }.apply { users.add(entity) }
    }

    override fun deleteById(id: Long) {
        users.removeIf { it.id == id }
    }

    override fun clear() {
        users.clear()
        tokens.clear()
    }

    override fun getTokenByTokenValidationInfo(
        tokenValidationInfo: TokenValidationInfo,
    ): Pair<User, Token>? =
        tokens
            .firstOrNull { it.tokenValidationInfo == tokenValidationInfo }
            ?.let {
                val user = findById(it.userId)
                requireNotNull(user)
                user to it
            }

    override fun createToken(
        token: Token,
        maxTokens: Int,
    ) {
        val nrOfTokens = tokens.count { it.userId == token.userId }
        if (nrOfTokens >= maxTokens) {
            tokens
                .filter { it.userId == token.userId }
                .minByOrNull { it.lastUsedAt }!!
                .also { tk -> tokens.removeIf { it.tokenValidationInfo == tk.tokenValidationInfo } }
        }
        tokens.add(token)
    }

    override fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    ) {
        tokens.removeIf { it.tokenValidationInfo == token.tokenValidationInfo }
        tokens.add(token)
    }

    override fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int {
        val count = tokens.count { it.tokenValidationInfo == tokenValidationInfo }
        tokens.removeAll { it.tokenValidationInfo == tokenValidationInfo }
        return count
    }
}
