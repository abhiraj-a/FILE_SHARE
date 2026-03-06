package FileManager.FileManager.ServiceImpl;
import FileManager.FileManager.DTO.FileTransferDTO;
import FileManager.FileManager.Entity.FileEntity;
import FileManager.FileManager.Entity.FileTransfer;
import FileManager.FileManager.Entity.User;
import FileManager.FileManager.ExceptionHandler.*;
import FileManager.FileManager.Service.FileService;
import FileManager.FileManager.Service.StorageService;
import FileManager.FileManager.Utils.ClerkUserPrincipal;
import FileManager.FileManager.Repository.FileEntityRepo;
import FileManager.FileManager.Repository.FileTransferRepo;
import FileManager.FileManager.Repository.UserRepo;
import FileManager.FileManager.Service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final FileEntityRepo fileEntityRepo;
    private final UserRepo userRepo;
    private final FileTransferRepo fileTransferRepo;
    private final StorageService storageService;
    private final FileService fileService;

    @Override
    @Transactional
    public FileTransferDTO transferFiles(ClerkUserPrincipal principal, List<UUID> fileIds) {

        User owner = userRepo.findByClerkId(principal.getClerkId()).orElseThrow(()->
         new RuntimeException("User not found"));

        List<FileEntity> files = fileEntityRepo.findAllById(fileIds);
        if(files.size()!=fileIds.size()) throw new FileNotFoundException();

        for(FileEntity f : files){
            if(!f.getOwner().getId().equals(owner.getId())) throw new ForbiddenException();
        }
        FileTransfer transfer = FileTransfer.builder()
                .owner(owner)
                .files(files)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(20)))
                .verificationCode(generateVerificationCode())
                .revoked(false)
                .build();

        fileTransferRepo.save(transfer);
        return FileTransferDTO.builder()
                        .transferId(transfer.getId())
                        .fileCount(files.size())
                        .verificationCode(transfer.getVerificationCode())
                        .build();
    }

    private String generateVerificationCode() {
        return String.format("%05d", new SecureRandom().nextInt(100000));
    }



//    @Cacheable(value = "receive-by-code" , key = "#verificationCode")
    @Transactional(readOnly = true)
    public FileTransferDTO recieveByCode(ClerkUserPrincipal principal, String verificationCode){

//        User receiver = userRepo.findByClerkId(principal.getClerkId()).orElseThrow(()->
//                new RuntimeException("User not found"));

        FileTransfer transfer = fileTransferRepo.findByVerificationCode(verificationCode)
                .orElseThrow(FileNotFoundException::new);

        if (transfer.getFiles().isEmpty()) {
            throw new RuntimeException("No files selected");
        }

        if(transfer.isRevoked()) throw new TransferRevokedException();

        if(transfer.getExpiresAt().isBefore(Instant.now())) throw new TransferExpiredException();

//        String fullUrl = "https://nnjgyyrhidboaqvdwrwc.supabase.co/storage/v1";


        return FileTransferDTO.builder()
                .verificationCode(verificationCode)
                .transferId(transfer.getId())
                .expiresAt(transfer.getExpiresAt())
                .fileMetaDataList(transfer.getFiles().stream().map(ft-> FileTransferDTO.FileMetaData.builder()
                        .id(ft.getId())
                        .fileSize(ft.getFileSize())
                        .originalFileName(ft.getOriginalFileName())
                        .contentType(ft.getContentType())
                        .build()).toList())
                .fileCount(transfer.getFiles().size())
                .downloads(transfer.getFiles().stream().map(ft->FileTransferDTO.FileDownload.builder()
                        .originalFileName(ft.getOriginalFileName())
                        .fileId(ft.getId())
                        .downloadUrl(storageService.generateSignedDownload(ft.getStoragePath(),ft.getOriginalFileName()))   //+"&response-content-disposition=attachment%3B%20filename%3D%22"
//                        + UriUtils.encode(ft.getOriginalFileName(), StandardCharsets.UTF_8)
//                        +"%22"+ "&response-content-type=application%2Foctet-stream")
                        .build()
                        ).toList())
                .build();
    }


    @Override
    @Transactional
    @CacheEvict(value = "receive-by-code" , allEntries = true)
    public FileTransferDTO downloadTransfer(ClerkUserPrincipal principal, String verificationCode) {

        FileTransfer transfer = fileTransferRepo.findByVerificationCode(verificationCode).orElseThrow(FileNotFoundException::new);

        if(transfer.getExpiresAt().isBefore(Instant.now())) throw new TransferExpiredException();

        if(transfer.isRevoked()) throw new TransferRevokedException();

//        String fullUrl = "https://nnjgyyrhidboaqvdwrwc.supabase.co/storage/v1";
        List<FileTransferDTO.FileDownload> downloads = transfer
                .getFiles().stream()
                .map(file->FileTransferDTO.FileDownload.builder()
                        .downloadUrl(storageService.generateSignedDownload(file.getStoragePath(),file.getOriginalFileName()))
                        .fileId(file.getId())
                        .originalFileName(file.getOriginalFileName())
                        .build()).toList();

        return FileTransferDTO.builder()
                .downloads(downloads)
                .fileCount(downloads.size())
                .transferId(transfer.getId())
                .build();
    }

    @Override
    @CacheEvict(value = "receive-by-code"   ,allEntries = true)
    public void revokeTransfer(ClerkUserPrincipal principal, UUID fileTransferId) {

        User owner  = userRepo.findByClerkId(principal.getClerkId()).orElseThrow(UserNotFoundException::new);
        FileTransfer transfer  = fileTransferRepo.findById(fileTransferId).orElseThrow(
                ()->new RuntimeException("Transfer not found"));

        if(!transfer.getOwner().getId().equals(owner.getId())) throw new ForbiddenException();

        if(!transfer.isRevoked()){
            transfer.setRevoked(true);
            fileTransferRepo.save(transfer);
        }
    }





}
