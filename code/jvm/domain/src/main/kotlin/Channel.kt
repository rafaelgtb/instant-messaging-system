package pt.isel

data class Channel(
    val id: Long,
    val name: String,
    val owner: User,
    val isPublic: Boolean = true,
)
