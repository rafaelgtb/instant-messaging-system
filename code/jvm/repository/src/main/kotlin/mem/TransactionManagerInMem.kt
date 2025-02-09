package pt.isel.mem

import pt.isel.Transaction
import pt.isel.TransactionManager

class TransactionManagerInMem : TransactionManager {
    private val repoUsers = UserRepositoryInMem()
    private val repoChannels = ChannelRepositoryInMem()
    private val repoMessages = MessageRepositoryInMem()
    private val repoMemberships = ChannelMemberRepositoryInMem()
    private val repoInvitations = InvitationRepositoryInMem()

    override fun <R> run(block: Transaction.() -> R): R =
        block(
            TransactionInMem(
                repoUsers,
                repoChannels,
                repoMessages,
                repoMemberships,
                repoInvitations,
            ),
        )
}
