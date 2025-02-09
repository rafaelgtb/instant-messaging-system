package pt.isel.auth

import kotlinx.datetime.Instant

class Token(
    val tokenValidationInfo: TokenValidationInfo,
    val userId: Long,
    val createdAt: Instant,
    val lastUsedAt: Instant,
)
