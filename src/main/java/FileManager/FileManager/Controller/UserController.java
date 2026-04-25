package FileManager.FileManager.Controller;

import FileManager.FileManager.Entity.User;
import FileManager.FileManager.ExceptionHandler.UserNotFoundException;
import FileManager.FileManager.Repository.UserRepo;
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
    private final UserRepo userRepo;

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

    @PostMapping("/temp/adding/{credits}")
    public ResponseEntity<?>  temp(@AuthenticationPrincipal ClerkUserPrincipal principal , @PathVariable("credits") int credits){
        User u = userRepo.findByClerkId(principal.getClerkId()).orElseThrow(UserNotFoundException::new);
        int c  = u.getCredits();
        u.setCredits(c+credits);
        userRepo.saveAndFlush(u);
        return ResponseEntity.ok().build();
    }
}
