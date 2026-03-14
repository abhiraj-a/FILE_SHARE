package FileManager.FileManager.ServiceImpl;
import FileManager.FileManager.DTO.FileEntityDTO;
import FileManager.FileManager.Entity.FileEntity;
import FileManager.FileManager.Entity.FileTransfer;
import FileManager.FileManager.Entity.User;
import FileManager.FileManager.ExceptionHandler.FileNotFoundException;
import FileManager.FileManager.ExceptionHandler.ForbiddenException;
import FileManager.FileManager.ExceptionHandler.UserNotFoundException;
import FileManager.FileManager.Repository.FileTransferRepo;
import FileManager.FileManager.Utils.ClerkUserPrincipal;
import FileManager.FileManager.Repository.FileEntityRepo;
import FileManager.FileManager.Repository.UserRepo;
import FileManager.FileManager.Service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final UserRepo userRepo;
    private  final FileEntityRepo fileEntityRepo;
    private final StorageServiceImpl storageServiceImpl;
    private final FileTransferRepo fileTransferRepo;
//


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "files" , key = "#principal.clerkId")
    public List<FileEntityDTO> getAllFiles(ClerkUserPrincipal principal) {

        User owner = userRepo.findByClerkId(principal.getClerkId()).orElseThrow(UserNotFoundException::new);

        List<FileEntity> files = fileEntityRepo.findAllByOwnerIdOrderByCreatedAtDesc(owner.getId());

        List<FileEntityDTO> dtos =new ArrayList<>();

        for (FileEntity file : files){
            dtos.add(FileEntityDTO.builder().originalFileName(file.getOriginalFileName())
                            .fileSize(file.getFileSize())
                            .uploadedAt(file.getCreatedAt())
                            .fileId(file.getId())
                            .build());
        }

        return dtos;
    }

//    @Override
//    @CacheEvict(value = "files" ,key = "#principal.clerkId")
//    @Transactional
//    public List<FileEntityDTO> uploadFiles(ClerkUserPrincipal principal, List<MultipartFile> multipartFiles) {
//        User owner = userRepo.findByClerkId(principal.getClerkId()).orElseThrow(UserNotFoundException::new);
//
//        List<FileEntityDTO> dtos =new ArrayList<>();
//
//        for (MultipartFile file:multipartFiles) {
//            if(file.isEmpty()) continue;
//            String storedfileName =UUID.randomUUID()+"_"+file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
//            String path =owner.getId()+"/"+storedfileName;
//            String storagePath = storageServiceImpl.upload(file,owner.getId() , path);
//            FileEntity fileEntity = FileEntity.builder()
//                    .originalFileName(file.getOriginalFilename())
//                    .fileType(file.getContentType())
//                    .fileSize(file.getSize())
//                    .createdAt(Instant.now())
//                    .storagePath(storagePath)
//                    .contentType(file.getContentType())
//                    .storedFileName(storedfileName)
//                    .owner(owner)
//                    .build();
//          FileEntity savedFile  =  fileEntityRepo.save(fileEntity);
//
//          dtos.add(FileEntityDTO.builder()
//                  .uploadedAt(savedFile.getCreatedAt())
//                  .fileId(savedFile.getId())
//                  .originalFileName(savedFile.getOriginalFileName())
//                  .fileSize(savedFile.getFileSize())
//                  .build());
//        }
//
//        return dtos;
//    }

    @Override
    public String download(ClerkUserPrincipal principal, UUID id) {
        User owner = userRepo.findByClerkId(principal.getClerkId()).orElseThrow(UserNotFoundException::new);
        FileEntity file  = fileEntityRepo.findById(id).orElseThrow(FileNotFoundException::new);
        if(!file.getOwner().getId().equals(owner.getId())) throw new RuntimeException("Access denied");
        return storageServiceImpl.generateSignedDownload(file.getStoragePath(), file.getOriginalFileName());
    }


//
//    @Override
//    public DownloadFileDTO downloadFile(ClerkUserPrincipal principal , UUID id){
//        User owner = userRepo.findByClerkId(principal.getClerkId()).orElseThrow(UserNotFoundException::new);
//
//        FileEntity file  = fileEntityRepo.findById(id).orElseThrow(FileNotFoundException::new);
//
//        if(!file.getOwner().getId().equals(owner.getId())) throw new RuntimeException("Access denied");
//
//          String signedDownload = storageService.generateSignedDownload(file.getStoragePath(), file.getOriginalFileName());
//        String fullurl = "https://nnjgyyrhidboaqvdwrwc.supabase.co/storage/v1"+signedDownload;
//        Resource resource =webClient.get()
//                .uri(fullurl)
//                .retrieve()
//                .bodyToMono(Resource.class)
//              .block();
//
//        return DownloadFileDTO.builder()
//                .resource(null)
//                .originalFileName(file.getOriginalFileName())
//                .content_type(file.getContentType())
//                .build();
//
//    }
//

    @Override
    @Transactional
    @CacheEvict(value = "files" , key = "#principal.clerkId")
    public void delete(ClerkUserPrincipal principal, List<UUID> ids) {
        User owner = userRepo.findByClerkId(principal.getClerkId()).orElseThrow(UserNotFoundException::new);


        List<FileEntity> filesToDelete = fileEntityRepo.findAllById(ids);
        for (FileEntity f:filesToDelete){
            if(!f.getOwner().getId().equals(owner.getId())) throw new ForbiddenException();
        }

        for (FileEntity file  : filesToDelete) {
            List<FileTransfer> transfers = fileTransferRepo.findAllByFiles_Id(file.getId());
            for (FileTransfer transfer : transfers) {
                transfer.getFiles().remove(file);
            }
            fileTransferRepo.saveAll(transfers);
        }

            for (FileEntity file  : filesToDelete){
            String p = file.getStoragePath();
            storageServiceImpl.delete(p);
        }
        fileEntityRepo.deleteAll(filesToDelete);
    }
}
