package pt.isel

import java.time.LocalDateTime

data class Invitation(
    val id: Long,
    val token: String,
    val createdBy: User,
    val channel: Channel,
    val accessType: AccessType,
    val expiresAt: LocalDateTime,
    val status: Status = Status.PENDING,
)
