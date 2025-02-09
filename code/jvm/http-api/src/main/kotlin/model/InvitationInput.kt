package pt.isel.model

import java.time.LocalDateTime
import pt.isel.AccessType

data class InvitationInput(
    val accessType: AccessType,
    val expiresAt: LocalDateTime,
)
