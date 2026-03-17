package FileManager.FileManager.Controller;

import FileManager.FileManager.Utils.ClerkUserPrincipal;
import FileManager.FileManager.Service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?>  signUp(@AuthenticationPrincipal ClerkUserPrincipal principal){
        return ResponseEntity.ok(userService.signup(principal));
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal ClerkUserPrincipal principal) {

        log.warn("Controller reached");
        userService.deleteUserByClerkId(principal.getClerkId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/credits")
    public ResponseEntity<?> getCredits(@AuthenticationPrincipal ClerkUserPrincipal principal){
        return ResponseEntity.ok(userService.getCredits(principal));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUser(@AuthenticationPrincipal ClerkUserPrincipal principal){
        return ResponseEntity.ok(userService.getUser(principal));
    }
}
