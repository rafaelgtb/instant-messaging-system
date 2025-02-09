package pt.isel

/** Repository interface for managing channels, extends the generic Repository */
interface ChannelRepository : Repository<Channel> {
    fun create(
        name: String,
        owner: User,
        isPublic: Boolean,
    ): Channel

    fun findByName(name: String): Channel?

    fun findAllByOwner(owner: User): List<Channel>

    fun findAllPublicChannels(
        limit: Int = 50,
        offset: Int = 0,
    ): List<Channel>

    fun searchByName(
        query: String,
        limit: Int = 50,
        offset: Int = 0,
    ): List<Channel>
}
