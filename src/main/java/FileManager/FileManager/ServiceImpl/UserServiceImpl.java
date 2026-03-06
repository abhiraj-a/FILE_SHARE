package FileManager.FileManager.ServiceImpl;

import FileManager.FileManager.DTO.UserDTO;
import FileManager.FileManager.Entity.FileEntity;
import FileManager.FileManager.Entity.User;
import FileManager.FileManager.Repository.FileTransferRepo;
import FileManager.FileManager.Service.StorageService;
import FileManager.FileManager.Utils.ClerkClient;
import FileManager.FileManager.Utils.ClerkUserPrincipal;
import FileManager.FileManager.Repository.FileEntityRepo;
import FileManager.FileManager.Repository.UserRepo;
import FileManager.FileManager.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;

    private final FileEntityRepo fileEntityRepo;

    private final ClerkClient client;

    private final FileTransferRepo fileTransferRepo;

    private final StorageService storageService;

    @Override
    @Transactional
    public UserDTO signup(ClerkUserPrincipal principal) {

      User newUser  = userRepo.findByClerkId(principal.getClerkId()).orElseGet(()->create(principal));
        return toDTO(newUser);
    }

    @Override
    @Transactional
    public void deleteUserByClerkId(String clerkId) {
        User user = userRepo.findByClerkId(clerkId).orElseThrow(()->new RuntimeException("User not found"));

        int joinRows = fileTransferRepo.deleteJoinRowsByUser(user.getId());
        System.out.println("Deleted join rows = " + joinRows);
        fileTransferRepo.flush();

        List<FileEntity> files = fileEntityRepo.findAllByOwnerId(user.getId());
        for (FileEntity f : files) {
            storageService.delete(f.getStoragePath());
        }
        fileEntityRepo.deleteFilesByUser(user.getId());
        fileEntityRepo.flush();
        userRepo.delete(user);
    }

    @Override
    public void requestAccountDeletion(ClerkUserPrincipal principal) {
        client.deleteUser(principal);
    }


    private User create(ClerkUserPrincipal principal){
        User newUser = User.builder()
                .email(principal.getEmail())
                .clerkId(principal.getClerkId())
//              .name(principal.getName())
                .build();

        return userRepo.save(newUser);
    }

    private UserDTO toDTO(User user){
        return UserDTO.builder().email(user.getEmail())
                .name(user.getName())
                .id(user.getId())
                .build();
    }
}
