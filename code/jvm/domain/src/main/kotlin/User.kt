package pt.isel

import pt.isel.auth.PasswordValidationInfo

data class User(
    val id: Long,
    val username: String,
    val passwordValidation: PasswordValidationInfo,
)
