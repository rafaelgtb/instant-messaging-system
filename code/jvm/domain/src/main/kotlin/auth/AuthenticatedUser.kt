package pt.isel.auth

import pt.isel.User

class AuthenticatedUser(
    val user: User,
    val token: String,
)
