package FileManager.FileManager.ServiceImpl;
import FileManager.FileManager.DTO.UserDTO;
import FileManager.FileManager.Entity.FileEntity;
import FileManager.FileManager.Entity.TriedEmail;
import FileManager.FileManager.Entity.User;
import FileManager.FileManager.ExceptionHandler.ApiException;
import FileManager.FileManager.Repository.FileTransferRepo;
import FileManager.FileManager.Repository.TriedEmailRepo;
import FileManager.FileManager.Service.StorageServiceImpl;
import FileManager.FileManager.Utils.ClerkClient;
import FileManager.FileManager.Utils.ClerkUserPrincipal;
import FileManager.FileManager.Repository.FileEntityRepo;
import FileManager.FileManager.Repository.UserRepo;
import FileManager.FileManager.Service.UserService;
import FileManager.FileManager.Utils.Hash;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepo userRepo;
    private final FileEntityRepo fileEntityRepo;
    private final ClerkClient client;
    private final FileTransferRepo fileTransferRepo;
    private final StorageServiceImpl storageServiceImpl;
    private final TriedEmailRepo triedEmailRepo;

    @Override
    @Transactional
    public UserDTO signup(ClerkUserPrincipal principal) {

        userRepo.findByClerkId(principal.getClerkId()).ifPresent(u->{
            if(u.isDeleted()){
                throw new ApiException("Account permanently deleted", HttpStatus.FORBIDDEN);
            }
        });
        boolean emailHasTrial= triedEmailRepo.existsByEmailHash(Hash.hash(principal.getEmail()));

      User newUser  = null;

      if(emailHasTrial){
          newUser = User.builder()
                  .email(principal.getEmail())
                  .credits(0)
                  .clerkId(principal.getClerkId())
                  .name(principal.getName())
                  .trialExpiresAt(Instant.now())
                  .build();
      }
      else {
          newUser = User.builder()
                  .email(principal.getEmail())
                  .credits(1000)
                  .name(principal.getName())
                  .clerkId(principal.getClerkId())
                  .build();
          TriedEmail triedEmail = TriedEmail.builder()
                  .emailHash(Hash.hash(newUser.getEmail()))
                  .firstSignupAt(Instant.now())
                  .build();
          triedEmailRepo.save(triedEmail);
      }
        userRepo.save(newUser);
        return toDTO(newUser);
    }

    @Override
    @Transactional
    public void deleteUserByClerkId(String clerkId) {
        User user = userRepo.findByClerkId(clerkId).orElseThrow(()->new RuntimeException("User not found"));

        int joinRows = fileTransferRepo.deleteJoinRowsByUser(user.getId());
//        System.out.println("Deleted join rows = " + joinRows);
        fileTransferRepo.flush();

        List<FileEntity> files = fileEntityRepo.findAllByOwnerId(user.getId());
        for (FileEntity f : files) {
            storageServiceImpl.delete(f.getStoragePath());
        }
        fileEntityRepo.deleteFilesByUser(user.getId());
        fileEntityRepo.flush();

        user.setDeleted(true);
        userRepo.save(user);
        client.deleteUser(new ClerkUserPrincipal(clerkId, user.getEmail(),user.getName()));

    }


    private UserDTO toDTO(User user){
        return UserDTO.builder().email(user.getEmail())
                .name(user.getName())
                .id(user.getId())
                .credits(user.getCredits())
                .build();
    }
}
