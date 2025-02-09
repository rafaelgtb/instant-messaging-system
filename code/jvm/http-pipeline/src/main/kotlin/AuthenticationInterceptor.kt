package pt.isel

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import pt.isel.auth.AuthenticatedUser

@Component
class AuthenticationInterceptor(
    private val authorizationHeaderProcessor: RequestTokenProcessor,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (
            handler is HandlerMethod &&
            handler.methodParameters.any { it.parameterType == AuthenticatedUser::class.java }
        ) {
            val user =
                request
                    .getHeader(NAME_AUTHORIZATION_HEADER)
                    ?.let { authorizationHeaderProcessor.processAuthorizationHeaderValue(it) }
                    ?: authorizationHeaderProcessor.processTokenFromCookie(request.cookies)

            return if (user == null) {
                response.apply {
                    status = 401
                    addHeader(NAME_WWW_AUTHENTICATE_HEADER, RequestTokenProcessor.SCHEME)
                }
                false
            } else {
                AuthenticatedUserArgumentResolver.addUserTo(user, request)
                true
            }
        }

        return true
    }

    companion object {
        const val NAME_AUTHORIZATION_HEADER = "Authorization"
        private const val NAME_WWW_AUTHENTICATE_HEADER = "WWW-Authenticate"
    }
}
