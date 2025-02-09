package pt.isel

/** Repository interface for managing channel members, extends the generic Repository */
interface ChannelMemberRepository : Repository<ChannelMember> {
    fun addUserToChannel(
        user: User,
        channel: Channel,
        accessType: AccessType,
    ): ChannelMember

    fun findUserInChannel(
        user: User,
        channel: Channel,
    ): ChannelMember?

    fun findAllChannelsForUser(
        user: User,
        limit: Int = 50,
        offset: Int = 0,
    ): List<ChannelMember>

    fun removeUserFromChannel(
        user: User,
        channel: Channel,
    )

    fun findAllMembersInChannel(
        channel: Channel,
        limit: Int = 50,
        offset: Int = 0,
    ): List<ChannelMember>
}
