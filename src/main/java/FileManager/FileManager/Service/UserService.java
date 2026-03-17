package FileManager.FileManager.Service;

import FileManager.FileManager.DTO.UserDTO;
import FileManager.FileManager.Utils.ClerkUserPrincipal;

import java.util.Map;

public interface UserService {
    UserDTO signup(ClerkUserPrincipal principal);

     void deleteUserByClerkId(String clerkId);

    Map<String,Integer> getCredits(ClerkUserPrincipal principal);

    UserDTO getUser(ClerkUserPrincipal principal);

//    void requestAccountDeletion(ClerkUserPrincipal principal);
}
