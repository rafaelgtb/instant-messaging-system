package pt.isel

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.auth.AuthenticatedUser
import pt.isel.model.PageInput
import pt.isel.model.RegisterInput
import pt.isel.model.UserHomeOutput
import pt.isel.model.UserInput

@RestController
@RequestMapping("/api")
class UserController(
    private val userService: UserService,
    private val channelService: ChannelService,
) {
    @PostMapping("/auth/register")
    fun registerUser(
        @RequestBody user: RegisterInput,
    ): ResponseEntity<*> =
        handleResult(userService.registerUser(user.username, user.password, user.invitationToken)) {
            ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/api/users/${it.id}")
                .build<Unit>()
        }

    @PostMapping("/auth/login")
    fun loginUser(
        @RequestBody user: UserInput,
    ): ResponseEntity<*> = handleResult(userService.createToken(user.username, user.password))

    @PostMapping("/auth/logout")
    fun logout(user: AuthenticatedUser) {
        userService.revokeToken(user.token)
    }

    @GetMapping("/users/me")
    fun userHome(user: AuthenticatedUser): ResponseEntity<UserHomeOutput> =
        ResponseEntity.ok(UserHomeOutput(user.user.id, user.user.username))

    @PostMapping("users/me")
    fun editUser(
        user: AuthenticatedUser,
        @RequestBody name: String,
    ): ResponseEntity<*> = handleResult(userService.updateUsername(user.user.id, name))

    @GetMapping("/users/me/channels")
    fun getUserChannels(
        user: AuthenticatedUser,
        @RequestParam page: PageInput = PageInput(),
    ): ResponseEntity<*> =
        handleResult(channelService.getJoinedChannels(user.user.id, page.limit, page.offset))

    @PostMapping("/users/me/delete")
    fun deleteUser(user: AuthenticatedUser): ResponseEntity<*> =
        handleResult(userService.deleteUser(user.user.id))
}
