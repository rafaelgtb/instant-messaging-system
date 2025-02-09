package pt.isel

import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import pt.isel.auth.AuthenticatedUser
import pt.isel.auth.PasswordValidationInfo
import pt.isel.auth.Sha256TokenEncoder
import pt.isel.auth.Token
import pt.isel.auth.TokenEncoder
import pt.isel.auth.TokenValidationInfo
import pt.isel.auth.UsersDomain
import pt.isel.auth.UsersDomainConfig

class InstantMessagingTest {
    private fun newTokenValidationData() = "token-${abs(Random.nextLong())}"

    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var tokenEncoder: TokenEncoder
    private lateinit var clock: Clock
    private lateinit var config: UsersDomainConfig
    private lateinit var usersDomain: UsersDomain

    @BeforeEach
    fun setup() {
        passwordEncoder = BCryptPasswordEncoder()
        tokenEncoder = Sha256TokenEncoder()
        clock = Clock.System
        config =
            UsersDomainConfig(
                tokenSizeInBytes = 256 / 8,
                tokenTtl = 24.hours,
                tokenRollingTtl = 1.hours,
                maxTokensPerUser = 3,
            )
        usersDomain = UsersDomain(passwordEncoder, tokenEncoder, config)
    }

    @Test
    fun `test authenticated user creation`() {
        val user = User(1, "user1", PasswordValidationInfo("password"))
        val authUser = AuthenticatedUser(user, "token")

        assertEquals(user, authUser.user)
        assertEquals("token", authUser.token)
    }

    @Test
    fun `test channel creation`() {
        val user = User(1, "user1", PasswordValidationInfo("password"))
        val channel = Channel(1, "public1", user)

        assertEquals(1, channel.id)
        assertEquals("public1", channel.name)
        assertEquals(user, channel.owner)
        assertTrue(channel.isPublic) // default value
    }

    @Test
    fun `test channel member creation`() {
        val user = User(1, "user1", PasswordValidationInfo("password"))
        val channel = Channel(1, "public1", user)
        val member = ChannelMember(1, user, channel, AccessType.READ_ONLY)

        assertEquals(1, member.id)
        assertEquals(user, member.user)
        assertEquals(channel, member.channel)
        assertEquals(AccessType.READ_ONLY, member.accessType)
    }

    @Test
    fun `test invitation creation`() {
        val user = User(1, "user1", PasswordValidationInfo("password"))
        val channel = Channel(1, "public1", user)
        val now = LocalDateTime.now()
        val invitation = Invitation(1, "token", user, channel, AccessType.READ_WRITE, now)

        assertEquals(1, invitation.id)
        assertEquals("token", invitation.token)
        assertEquals(user, invitation.createdBy)
        assertEquals(channel, invitation.channel)
        assertEquals(AccessType.READ_WRITE, invitation.accessType)
        assertEquals(Status.PENDING, invitation.status) // default value
    }

    @Test
    fun `test message creation`() {
        val user = User(1, "user1", PasswordValidationInfo("password"))
        val channel = Channel(1, "public1", user)
        val message = Message(1, "ola", user, channel)

        assertEquals(1, message.id)
        assertEquals("ola", message.content)
        assertEquals(user, message.user)
        assertEquals(channel, message.channel)
        assertNotNull(message.createdAt) // should not be null
    }

    @Test
    fun `test password validation info creation`() {
        val validationInfo = PasswordValidationInfo("password")
        assertEquals("password", validationInfo.validationInfo)
    }

    @Test
    fun `test token creation`() {
        val tokenValidationInfo = tokenEncoder.createValidationInformation(newTokenValidationData())
        val userId = 1L
        val createdAt = clock.now()
        val lastUsedAt = clock.now()

        val token = Token(tokenValidationInfo, userId, createdAt, lastUsedAt)

        assertEquals(tokenValidationInfo, token.tokenValidationInfo)
        assertEquals(userId, token.userId)
        assertEquals(createdAt, token.createdAt)
        assertEquals(lastUsedAt, token.lastUsedAt)
    }

    @Test
    fun `test token validation info creation`() {
        val tokenValidationInfo = TokenValidationInfo("validationInfo")
        assertEquals("validationInfo", tokenValidationInfo.validationInfo)
    }

    @Test
    fun `test user creation`() {
        val user = User(1, "user1", PasswordValidationInfo("password"))

        assertEquals(1, user.id)
        assertEquals("user1", user.username)
        assertEquals("password", user.passwordValidation.validationInfo)
    }

    @Test
    fun `test generate token value`() {
        val token = usersDomain.generateTokenValue()
        assertTrue(usersDomain.canBeToken(token))
    }

    @Test
    fun `test create token validation information`() {
        val token = usersDomain.generateTokenValue()
        val tokenValidationInfo = usersDomain.createTokenValidationInformation(token)
        assertNotNull(tokenValidationInfo)
    }

    @Test
    fun `test validate token`() {
        val token = usersDomain.generateTokenValue()
        val tokenValidationInfo = usersDomain.createTokenValidationInformation(token)
        val userId = 1L
        val createdAt = clock.now()
        val lastUsedAt = clock.now()
        val tokenObj = Token(tokenValidationInfo, userId, createdAt, lastUsedAt)

        assertTrue(usersDomain.isTokenTimeValid(clock, tokenObj))
    }

    @Test
    fun `test can be token`() {
        val validToken = usersDomain.generateTokenValue()

        assertTrue(usersDomain.canBeToken(validToken))
        assertFalse(usersDomain.canBeToken("shortToken"))
        assertFalse(usersDomain.canBeToken(""))
    }

    @Test
    fun `test validate password`() {
        val validationInfo = usersDomain.createPasswordValidationInformation("password123!")

        assertTrue(usersDomain.validatePassword("password123!", validationInfo))
        assertFalse(usersDomain.validatePassword("wrongPassword", validationInfo))
    }

    @Test
    fun `test create password validation information`() {
        val password = "Password!"
        val validationInfo = usersDomain.createPasswordValidationInformation(password)

        assertNotNull(validationInfo)
        assertTrue(passwordEncoder.matches(password, validationInfo.validationInfo))
    }

    @Test
    fun `test is safe password`() {
        assertTrue(usersDomain.isSafePassword("SafePassword123!"))
        assertFalse(usersDomain.isSafePassword("short"))
        assertFalse(usersDomain.isSafePassword("nocapital123"))
        assertFalse(usersDomain.isSafePassword("NOLOWERCASE123"))
        assertFalse(usersDomain.isSafePassword("NoNumber!"))
        assertFalse(usersDomain.isSafePassword("NoSpecialChar123"))
    }

    @Test
    fun `test get token expiration`() {
        val now = Clock.System.now()

        val token1 = Token(TokenValidationInfo("info"), 1L, now - 30.hours, now - 30.minutes)
        val expiration1 = usersDomain.getTokenExpiration(token1)
        assertEquals(token1.createdAt + config.tokenTtl, expiration1)

        val token2 = Token(TokenValidationInfo("info"), 1L, now - 30.minutes, now - 30.hours)
        val expiration2 = usersDomain.getTokenExpiration(token2)
        assertEquals(token2.lastUsedAt + config.tokenRollingTtl, expiration2)
    }

    @Test
    fun `test users domain config creation`() {
        val config = UsersDomainConfig(256 / 8, 24.hours, 1.hours, 3)

        assertEquals(256 / 8, config.tokenSizeInBytes)
        assertEquals(24.hours, config.tokenTtl)
        assertEquals(1.hours, config.tokenRollingTtl)
        assertEquals(3, config.maxTokensPerUser)
    }

    @Test
    fun `test users domain config creation with invalid values`() {
        assertFails { UsersDomainConfig(0, 1.hours, 30.minutes, 5) }
        assertFails { UsersDomainConfig(32, Duration.ZERO, 30.minutes, 5) }
        assertFails { UsersDomainConfig(32, 1.hours, Duration.ZERO, 5) }
        assertFails { UsersDomainConfig(32, 1.hours, 30.minutes, 0) }
    }

    @Test
    fun `is token time valid`() {
        val now = clock.now()
        val validationInfo = TokenValidationInfo("info")

        val token1 = Token(validationInfo, 1L, now - 30.minutes, now - 15.minutes)
        assertTrue(usersDomain.isTokenTimeValid(clock, token1))

        val token2 = Token(validationInfo, 1L, now - 30.minutes, now - 15.hours)
        assertFalse(usersDomain.isTokenTimeValid(clock, token2))

        val token3 = Token(validationInfo, 1L, now - 30.hours, now - 15.minutes)
        assertFalse(usersDomain.isTokenTimeValid(clock, token3))
    }

    @Test
    fun `test Sha256TokenEncoder create validation information`() {
        val tokenEncoder = Sha256TokenEncoder()
        val token = "testToken"
        val tokenValidationInfo = tokenEncoder.createValidationInformation(token)
        assertNotNull(tokenValidationInfo)
        assertEquals(
            tokenValidationInfo.validationInfo,
            tokenEncoder.createValidationInformation(token).validationInfo,
        )
    }
}
