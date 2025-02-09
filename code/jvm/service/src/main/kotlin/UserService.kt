package pt.isel

import jakarta.inject.Named
import java.time.LocalDateTime
import kotlinx.datetime.Clock
import pt.isel.auth.Token
import pt.isel.auth.TokenExternalInfo
import pt.isel.auth.UsersDomain

@Named
class UserService(
    private val trxManager: TransactionManager,
    private val usersDomain: UsersDomain,
    private val clock: Clock,
) {
    fun registerUser(
        username: String,
        password: String,
        token: String? = null,
    ): Either<UserError, User> =
        trxManager.run {
            if (repoUsers.findAll().isEmpty()) {
                return@run createUser(username, password)
            }
            if (token.isNullOrBlank()) return@run failure(UserError.EmptyToken)

            val invitation =
                repoInvitations.findByToken(token)
                    ?: return@run failure(UserError.InvitationNotFound)

            if (invitation.expiresAt.isBefore(LocalDateTime.now())) {
                return@run failure(UserError.InvitationExpired)
            }
            if (invitation.status != Status.PENDING) {
                return@run failure(UserError.InvitationAlreadyUsed)
            }

            val newUser = createUser(username, password)
            if (newUser is Either.Left) {
                return@run failure(newUser.value)
            } else if (newUser is Either.Right) {
                repoMemberships.addUserToChannel(
                    newUser.value,
                    invitation.channel,
                    invitation.accessType,
                )
                repoInvitations.save(invitation.copy(status = Status.ACCEPTED))
                return@run success(newUser.value)
            }
            newUser
        }

    private fun Transaction.createUser(
        username: String,
        password: String,
    ): Either<UserError, User> {
        if (username.isBlank()) return failure(UserError.EmptyUsername)
        if (password.isBlank()) return failure(UserError.EmptyPassword)

        if (repoUsers.findByUsername(username) != null) {
            return failure(UserError.UsernameAlreadyInUse)
        }
        if (!usersDomain.isSafePassword(password)) {
            return failure(UserError.InsecurePassword)
        }

        val passwordValidationInfo = usersDomain.createPasswordValidationInformation(password)
        val newUser = repoUsers.create(username, passwordValidationInfo)

        return success(newUser)
    }

    fun getUserById(userId: Long): Either<UserError, User> =
        trxManager.run {
            repoUsers.findById(userId)?.let { success(it) }
                ?: return@run failure(UserError.UserNotFound)
        }

    fun getUserByUsername(username: String): Either<UserError, User> {
        if (username.isBlank()) return failure(UserError.EmptyUsername)

        return trxManager.run {
            repoUsers.findByUsername(username)?.let { success(it) }
                ?: return@run failure(UserError.UserNotFound)
        }
    }

    fun updateUsername(
        userId: Long,
        newUsername: String,
        password: String = "",
    ): Either<UserError, User> {
        if (newUsername.isBlank()) return failure(UserError.EmptyUsername)

        return trxManager.run {
            val user = repoUsers.findById(userId) ?: return@run failure(UserError.UserNotFound)

            if (repoUsers.findByUsername(newUsername) != null) {
                return@run failure(UserError.UsernameAlreadyInUse)
            }
            // if (!usersDomain.validatePassword(password, user.passwordValidation)) {
            //    return@run failure(UserError.IncorrectPassword)
            // }

            val updatedUser = user.copy(username = newUsername)
            repoUsers.save(updatedUser)
            success(updatedUser)
        }
    }

    fun updatePassword(
        userId: Long,
        newPassword: String,
    ): Either<UserError, User> {
        if (newPassword.isBlank()) return failure(UserError.EmptyPassword)
        if (!usersDomain.isSafePassword(newPassword)) {
            return failure(UserError.InsecurePassword)
        }

        return trxManager.run {
            val user = repoUsers.findById(userId) ?: return@run failure(UserError.UserNotFound)

            if (usersDomain.validatePassword(newPassword, user.passwordValidation)) {
                return@run failure(UserError.PasswordSameAsPrevious)
            }

            val passwordValidation = usersDomain.createPasswordValidationInformation(newPassword)
            val updatedUser = user.copy(passwordValidation = passwordValidation)
            repoUsers.save(updatedUser)
            success(updatedUser)
        }
    }

    fun deleteUser(userId: Long): Either<UserError, String> =
        trxManager.run {
            val user = repoUsers.findById(userId) ?: return@run failure(UserError.UserNotFound)

            if (repoChannels.findAllByOwner(user).isNotEmpty()) {
                return@run failure(UserError.UserHasOwnedChannels)
            }

            repoMemberships.findAllChannelsForUser(user, Int.MAX_VALUE, 0).forEach {
                repoMemberships.removeUserFromChannel(user, it.channel)
            }

            repoUsers.deleteById(userId)
            success("User ${user.id} deleted")
        }

    fun createToken(
        username: String,
        password: String,
    ): Either<UserError, TokenExternalInfo> {
        if (username.isBlank()) return failure(UserError.EmptyUsername)
        if (password.isBlank()) return failure(UserError.EmptyPassword)

        return trxManager.run {
            val user =
                repoUsers.findByUsername(username) ?: return@run failure(UserError.UserNotFound)

            if (!usersDomain.validatePassword(password, user.passwordValidation)) {
                return@run failure(UserError.IncorrectPassword)
            }

            val tokenValue = usersDomain.generateTokenValue()
            val now = clock.now()
            val newToken =
                Token(
                    usersDomain.createTokenValidationInformation(tokenValue),
                    user.id,
                    createdAt = now,
                    lastUsedAt = now,
                )

            repoUsers.createToken(newToken, usersDomain.maxNumberOfTokensPerUser)
            Either.Right(TokenExternalInfo(tokenValue, usersDomain.getTokenExpiration(newToken)))
        }
    }

    fun getUserByToken(token: String): User? {
        if (!usersDomain.canBeToken(token)) return null

        return trxManager.run {
            val tokenValidationInfo = usersDomain.createTokenValidationInformation(token)

            repoUsers.getTokenByTokenValidationInfo(tokenValidationInfo)?.let { (user, token) ->
                if (usersDomain.isTokenTimeValid(clock, token)) {
                    repoUsers.updateTokenLastUsed(token, clock.now())
                    user
                } else {
                    null
                }
            }
        }
    }

    fun revokeToken(token: String): Boolean {
        val tokenValidationInfo = usersDomain.createTokenValidationInformation(token)

        return trxManager.run {
            repoUsers.removeTokenByValidationInfo(tokenValidationInfo)
            true
        }
    }
}
