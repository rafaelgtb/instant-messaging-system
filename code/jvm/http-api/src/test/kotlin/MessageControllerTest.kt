package pt.isel

import java.util.stream.Stream
import kotlin.test.assertEquals
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.http.HttpStatus
import pt.isel.auth.AuthenticatedUser
import pt.isel.auth.PasswordValidationInfo
import pt.isel.mem.TransactionManagerInMem
import pt.isel.model.MessageRequest
import pt.isel.model.PageInput

class MessageControllerTest {
    companion object {
        private val jdbi =
            Jdbi
                .create(PGSimpleDataSource().apply { setURL(Environment.getDbUrl()) })
                .configureWithAppRequirements()

        @JvmStatic
        fun transactionManagers(): Stream<TransactionManager> =
            Stream.of(
                TransactionManagerInMem().also { cleanup(it) },
                TransactionManagerJdbi(jdbi).also { cleanup(it) },
            )

        private fun cleanup(trxManager: TransactionManager) {
            trxManager.run {
                repoUsers.clear()
                repoMemberships.clear()
                repoChannels.clear()
                repoMessages.clear()
                repoInvitations.clear()
            }
        }
    }

    private lateinit var trxManager: TransactionManager
    private lateinit var messageEventService: MessageEventService
    private lateinit var messageService: MessageService
    private lateinit var messageController: MessageController

    @BeforeEach
    fun setup() {
        trxManager = TransactionManagerInMem()
        messageEventService = MessageEventService(trxManager)
        messageService = MessageService(trxManager, messageEventService)
        messageController = MessageController(messageService)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `createMessage - Success`() {
        trxManager.run {
            val user = repoUsers.create("user", PasswordValidationInfo("hash"))
            val channel = repoChannels.create("channel", user, true)
            repoMemberships.addUserToChannel(user, channel, AccessType.READ_WRITE)
            val authUser = AuthenticatedUser(user, "token")
            val message = MessageRequest("ola!")
            val response = messageController.createMessage(authUser, channel.id, message)

            assertEquals(HttpStatus.OK, response.statusCode)
            assert(response.body is Message)
        }
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `createMessage - Failure Invalid message content`() {
        trxManager.run {
            val user = repoUsers.create("user", PasswordValidationInfo("hash"))
            val channel = repoChannels.create("channel", user, true)
            repoMemberships.addUserToChannel(user, channel, AccessType.READ_WRITE)
            val authUser = AuthenticatedUser(user, "token")
            val message = MessageRequest("")
            val response = messageController.createMessage(authUser, channel.id, message)

            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
            assertEquals(Problem.EmptyMessage.title, (response.body as ProblemResponse).title)
        }
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `createMessage - Failure User not found`() {
        trxManager.run {
            val user1 = repoUsers.create("user1", PasswordValidationInfo("hash"))
            val user2 = User(2, "user2", PasswordValidationInfo("hash"))
            val channel = repoChannels.create("channel", user1, true)
            repoMemberships.addUserToChannel(user1, channel, AccessType.READ_WRITE)
            val authUser = AuthenticatedUser(user2, "token")
            val message = MessageRequest("ola!")
            val response = messageController.createMessage(authUser, channel.id, message)

            assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
            assertEquals(Problem.UserNotFound.title, (response.body as ProblemResponse).title)
        }
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `createMessage - Failure Channel not found`() {
        trxManager.run {
            val user = repoUsers.create("user", PasswordValidationInfo("hash"))
            val channel = Channel(1, "channel", user, true)
            val authUser = AuthenticatedUser(user, "token")
            val message = MessageRequest("ola!")
            val response = messageController.createMessage(authUser, channel.id, message)

            assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
            assertEquals(Problem.ChannelNotFound.title, (response.body as ProblemResponse).title)
        }
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `createMessage - Failure User not in channel`() {
        trxManager.run {
            val user1 = repoUsers.create("user1", PasswordValidationInfo("hash"))
            val user2 = repoUsers.create("user2", PasswordValidationInfo("hash"))
            val channel = repoChannels.create("channel", user1, true)
            repoMemberships.addUserToChannel(user1, channel, AccessType.READ_WRITE)
            val authUser = AuthenticatedUser(user2, "token")
            val message = MessageRequest("ola!")
            val response = messageController.createMessage(authUser, channel.id, message)

            assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
            assertEquals(Problem.UserNotInChannel.title, (response.body as ProblemResponse).title)
        }
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `createMessage - Failure Write permission denied`() {
        trxManager.run {
            val user1 = repoUsers.create("user1", PasswordValidationInfo("hash"))
            val user2 = repoUsers.create("user2", PasswordValidationInfo("hash"))
            val channel = repoChannels.create("channel", user1, false)
            repoMemberships.addUserToChannel(user1, channel, AccessType.READ_WRITE)
            repoMemberships.addUserToChannel(user2, channel, AccessType.READ_ONLY)
            val authUser = AuthenticatedUser(user2, "token")
            val message = MessageRequest("ola!")
            val response = messageController.createMessage(authUser, channel.id, message)

            assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
            assertEquals(Problem.UserNotAuthorized.title, (response.body as ProblemResponse).title)
        }
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getMessages - Success`() {
        trxManager.run {
            val user = repoUsers.create("user", PasswordValidationInfo("hash"))
            val channel = repoChannels.create("channel", user, false)
            repoMemberships.addUserToChannel(user, channel, AccessType.READ_WRITE)
            val authUser = AuthenticatedUser(user, "token")
            (1..5).map {
                val message = MessageRequest("ola-$it!")
                messageController.createMessage(authUser, channel.id, message)
            }
            val response = messageController.getMessages(authUser, channel.id)

            assertEquals(HttpStatus.OK, response.statusCode)
            // assertEquals(messages, response.body)
        }
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getMessages - Failure Invalid limit`() {
        trxManager.run {
            val user = repoUsers.create("user", PasswordValidationInfo("hash"))
            val channel = repoChannels.create("channel", user, false)
            repoMemberships.addUserToChannel(user, channel, AccessType.READ_WRITE)
            val authUser = AuthenticatedUser(user, "token")
            (1..5).map {
                val message = MessageRequest("ola-$it!")
                messageController.createMessage(authUser, channel.id, message)
            }
            val response = messageController.getMessages(authUser, channel.id, PageInput(limit = 0))

            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
            assertEquals(Problem.InvalidLimit.title, (response.body as ProblemResponse).title)
        }
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getMessages - Failure Invalid offset`() {
        trxManager.run {
            val user = repoUsers.create("user", PasswordValidationInfo("hash"))
            val channel = repoChannels.create("channel", user, false)
            repoMemberships.addUserToChannel(user, channel, AccessType.READ_WRITE)
            val authUser = AuthenticatedUser(user, "token")
            (1..5).map {
                val message = MessageRequest("ola-$it!")
                messageController.createMessage(authUser, channel.id, message)
            }
            val response =
                messageController.getMessages(authUser, channel.id, PageInput(offset = -1))

            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
            assertEquals(Problem.InvalidOffset.title, (response.body as ProblemResponse).title)
        }
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getMessages - Failure Channel not found`() {
        trxManager.run {
            val user = repoUsers.create("user", PasswordValidationInfo("hash"))
            val channel = Channel(1, "channel", user, false)
            val authUser = AuthenticatedUser(user, "token")
            val response = messageController.getMessages(authUser, channel.id)

            assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
            assertEquals(Problem.ChannelNotFound.title, (response.body as ProblemResponse).title)
        }
    }
}
