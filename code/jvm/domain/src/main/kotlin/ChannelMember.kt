package pt.isel

data class ChannelMember(
    val id: Long,
    val user: User,
    val channel: Channel,
    val accessType: AccessType,
)
