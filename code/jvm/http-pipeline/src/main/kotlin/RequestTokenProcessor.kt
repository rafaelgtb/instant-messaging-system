package pt.isel

import jakarta.servlet.http.Cookie
import org.springframework.stereotype.Component
import pt.isel.auth.AuthenticatedUser

@Component
class RequestTokenProcessor(
    private val usersService: UserService,
) {
    fun processAuthorizationHeaderValue(authorizationValue: String?): AuthenticatedUser? {
        if (authorizationValue == null) return null

        val parts = authorizationValue.trim().split(" ")
        if (parts.size != 2) return null
        if (parts[0].lowercase() != SCHEME) return null

        return usersService.getUserByToken(parts[1])?.let { AuthenticatedUser(it, parts[1]) }
    }

    fun processTokenFromCookie(cookies: Array<Cookie>?): AuthenticatedUser? {
        val token = cookies?.find { it.name == TOKEN_COOKIE_NAME }?.value
        return token?.let {
            usersService.getUserByToken(it)?.let { user -> AuthenticatedUser(user, it) }
        }
    }

    companion object {
        const val SCHEME = "bearer"
        const val TOKEN_COOKIE_NAME = "token"
    }
}
