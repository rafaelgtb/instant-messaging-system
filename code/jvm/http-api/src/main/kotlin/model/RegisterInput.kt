package pt.isel.model

data class RegisterInput(
    val username: String,
    val password: String,
    val invitationToken: String? = null,
)
