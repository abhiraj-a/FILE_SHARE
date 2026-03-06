package FileManager.FileManager.Service;

import FileManager.FileManager.DTO.UserDTO;
import FileManager.FileManager.Utils.ClerkUserPrincipal;

public interface UserService {
    UserDTO signup(ClerkUserPrincipal principal);

     void deleteUserByClerkId(String clerkId);

//    void requestAccountDeletion(ClerkUserPrincipal principal);
}
