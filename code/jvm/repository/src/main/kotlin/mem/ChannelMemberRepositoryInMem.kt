package pt.isel.mem

import jakarta.inject.Named
import pt.isel.AccessType
import pt.isel.Channel
import pt.isel.ChannelMember
import pt.isel.ChannelMemberRepository
import pt.isel.User

/**
 * Naif in memory repository non thread-safe and basic sequential id. Useful for unit tests purpose.
 */
@Named
class ChannelMemberRepositoryInMem : ChannelMemberRepository {
    private val channelMembers = mutableListOf<ChannelMember>()

    override fun addUserToChannel(
        user: User,
        channel: Channel,
        accessType: AccessType,
    ): ChannelMember =
        ChannelMember(channelMembers.size.toLong() + 1, user, channel, accessType).also {
            channelMembers.add(it)
        }

    override fun findById(id: Long): ChannelMember? = channelMembers.firstOrNull { it.id == id }

    override fun findUserInChannel(
        user: User,
        channel: Channel,
    ): ChannelMember? = channelMembers.firstOrNull { it.user == user && it.channel == channel }

    override fun findAllChannelsForUser(
        user: User,
        limit: Int,
        offset: Int,
    ): List<ChannelMember> = channelMembers.filter { it.user == user }.drop(offset).take(limit)

    override fun findAllMembersInChannel(
        channel: Channel,
        limit: Int,
        offset: Int,
    ): List<ChannelMember> =
        channelMembers.filter { it.channel == channel }.drop(limit).take(offset)

    override fun findAll(): List<ChannelMember> = channelMembers.toList()

    override fun save(entity: ChannelMember) {
        channelMembers.removeIf { it.id == entity.id }.apply { channelMembers.add(entity) }
    }

    override fun removeUserFromChannel(
        user: User,
        channel: Channel,
    ) {
        channelMembers.removeIf { it.user == user && it.channel == channel }
    }

    override fun deleteById(id: Long) {
        channelMembers.removeIf { it.id == id }
    }

    override fun clear() {
        channelMembers.clear()
    }
}
