package FileManager.FileManager.ServiceImpl;
import FileManager.FileManager.DTO.*;
import FileManager.FileManager.Entity.FileEntity;
import FileManager.FileManager.Entity.FileTransfer;
import FileManager.FileManager.Entity.User;
import FileManager.FileManager.ExceptionHandler.*;
import FileManager.FileManager.ServiceImpl.StorageServiceImpl;
import FileManager.FileManager.Utils.ClerkUserPrincipal;
import FileManager.FileManager.Repository.FileEntityRepo;
import FileManager.FileManager.Repository.FileTransferRepo;
import FileManager.FileManager.Repository.UserRepo;
import FileManager.FileManager.Utils.CreditCalculator;
import FileManager.FileManager.Utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl  {

    private final FileEntityRepo fileEntityRepo;
    private final UserRepo userRepo;
    private final FileTransferRepo fileTransferRepo;
    private final StorageServiceImpl storageServiceImpl;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    @Value("${S3_BUCKET_NAME}")
    private  String bucket;

//    @Transactional
//    public FileTransferDTO transferFiles(ClerkUserPrincipal principal, List<UUID> fileIds) {
//
//        User owner = userRepo.findByClerkId(principal.getClerkId()).orElseThrow(()->
//         new RuntimeException("User not found"));
//
//        List<FileEntity> files = fileEntityRepo.findAllById(fileIds);
//        if(files.size()!=fileIds.size()) throw new FileNotFoundException();
//
//        for(FileEntity f : files){
//            if(!f.getOwner().getId().equals(owner.getId())) throw new ForbiddenException();
//        }
//
//        boolean onTrial = owner.getTrialExpiresAt()!=null&&owner.getTrialExpiresAt().isAfter(Instant.now());
//
//        if(!onTrial){
//            int cost = files.stream().mapToInt(f->CreditCalculator.calculate(f.getFileSize())).sum();
//            if(owner.getCredits()<cost){
//                throw new InsufficientCreditException();
//            }
//            owner.setCredits(owner.getCredits()-cost);
//            userRepo.saveAndFlush(owner);
//        }
//        FileTransfer transfer = FileTransfer.builder()
//                .owner(owner)
//                .files(files)
//                .expiresAt(Instant.now().plus(Duration.ofMinutes(20)))
//                .verificationCode(generateVerificationCode())
//                .revoked(false)
//                .build();
//
//        fileTransferRepo.save(transfer);
//        return FileTransferDTO.builder()
//                        .transferId(transfer.getId())
//                        .fileCount(files.size())
//                        .verificationCode(transfer.getVerificationCode())
//                        .build();
//    }

    private String generateVerificationCode() {
        return String.format("%05d", new SecureRandom().nextInt(100000));
    }



//    @Cacheable(value = "receive-by-code" , key = "#verificationCode")
    @Transactional(readOnly = true)
    public FileTransferDTO recieveByCode(ClerkUserPrincipal principal, String verificationCode){

        FileTransfer transfer = fileTransferRepo.findByVerificationCode(verificationCode)
                .orElseThrow(FileNotFoundException::new);

        if (transfer.getFiles().isEmpty()) {
            throw new ApiException("No  files found ",HttpStatus.NOT_FOUND);
        }

        if(transfer.isRevoked()) throw new TransferRevokedException();

        if(transfer.getExpiresAt().isBefore(Instant.now())) throw new TransferExpiredException();

        if(!transfer.getStatus().equals("completed")) throw new TransferNotFoundException();

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
                        .downloadUrl(storageServiceImpl.generateSignedDownload(ft.getStoragePath(),ft.getOriginalFileName()))
                        .build()
                        ).toList())
                .build();
    }

    @Transactional
    @CacheEvict(value = "receive-by-code" , allEntries = true)
    public FileTransferDTO downloadTransfer(ClerkUserPrincipal principal, String verificationCode) {

        FileTransfer transfer = fileTransferRepo.findByVerificationCode(verificationCode).orElseThrow(FileNotFoundException::new);

        if(transfer.getExpiresAt().isBefore(Instant.now())) throw new TransferExpiredException();

        if(transfer.isRevoked()) throw new TransferRevokedException();
        List<FileTransferDTO.FileDownload> downloads = transfer
                .getFiles().stream()
                .map(file->FileTransferDTO.FileDownload.builder()
                        .downloadUrl(storageServiceImpl.generateSignedDownload(file.getStoragePath(),file.getOriginalFileName()))
                        .fileId(file.getId())
                        .originalFileName(file.getOriginalFileName())
                        .build()).toList();

        transfer.setDownloadedAt(Instant.now());
        fileTransferRepo.save(transfer);
        return FileTransferDTO.builder()
                .downloads(downloads)
                .fileCount(downloads.size())
                .transferId(transfer.getId())
                .build();
    }


    @CacheEvict(value = "receive-by-code"   ,allEntries = true)
    public void revokeTransfer(ClerkUserPrincipal principal, UUID fileTransferId) {

        User owner  = userRepo.findByClerkId(principal.getClerkId()).orElseThrow(UserNotFoundException::new);
        FileTransfer transfer  = fileTransferRepo.findById(fileTransferId).orElseThrow(
                TransferNotFoundException::new);

        if(!transfer.getOwner().getId().equals(owner.getId())) throw new ForbiddenException();

        if(!transfer.isRevoked()){
            transfer.setRevoked(true);
            fileTransferRepo.save(transfer);
        }
    }



    @Transactional
    public TransferInitResponse
    init(ClerkUserPrincipal principal, List<TransferinitDTO> files) {
        User owner  = userRepo.findByClerkId(principal.getClerkId()).orElseThrow(UserNotFoundException::new);
        boolean emailonTrial =principal.getEmail()!=null&& owner.getTrialExpiresAt().isAfter(Instant.now());
       if(!emailonTrial) {
           long totBytes = files.stream().mapToLong(TransferinitDTO::getFileSize).sum();
           int cost = CreditCalculator.calculate(totBytes);
           if (owner.getCredits() < cost) {
               throw new InsufficientCreditException();
           }
           owner.setCredits(owner.getCredits() - cost);
       }
        
       List<FileEntity> fileEntities = new ArrayList<>();
       List<PreSignedDTO> preSignedDTOS =new ArrayList<>();
       for (var f : files){
           String key = owner.getId()+"/"+ UUID.randomUUID()+"/"+f.getOriginalFileName();

           FileEntity fileEntity = FileEntity.builder()
                   .contentType(f.getContentType())
                   .originalFileName(f.getOriginalFileName())
                   .fileSize(f.getFileSize())
                   .storagePath(key)
                   .createdAt(Instant.now())
                   .owner(owner)
                   .build();

           PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                   .contentType(f.getContentType())
                   .bucket(bucket)
                   .key(key)
                   .contentLength(f.getFileSize())
                   .build();

           PutObjectPresignRequest presignRequest =PutObjectPresignRequest.builder()
                   .putObjectRequest(putObjectRequest)
                   .signatureDuration(Duration.ofMinutes(30))
                   .build();
           String url = s3Presigner.presignPutObject(presignRequest).url().toString();
          preSignedDTOS.add( PreSignedDTO.builder()
                  .url(url)
                  .originalFileName(f.getOriginalFileName())
                  .build());

          fileEntities.add(fileEntity);
       }

        FileTransfer transfer = FileTransfer.builder()
                .files(fileEntities)
                .verificationCode(generateVerificationCode())
                .owner(owner)
                .revoked(false)
                .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                .transferId(IdGenerator.generateTransferId())
                .status("pending")
                .build();

        fileTransferRepo.save(transfer);
        fileEntityRepo.saveAll(fileEntities);
       return TransferInitResponse.builder()
               .preSignedDTO(preSignedDTOS)
               .transferId(transfer.getTransferId())
               .build();
    }

    public TransferCompletionDTO confirm(ClerkUserPrincipal principal, String transferId) {
        FileTransfer transfer = fileTransferRepo.findByTransferId(transferId)
                .orElseThrow(TransferNotFoundException::new);
        if(!transfer.getOwner().getClerkId().equals(principal.getClerkId())){
            throw new ApiException("Id mismatch" , HttpStatus.UNAUTHORIZED);
        }

        if(transfer.getStatus().equals("completed")){
            return TransferCompletionDTO
                    .builder()
                    .expiresAt(Instant.now().plus(30,ChronoUnit.MINUTES))
                    .verificationCode(transfer.getVerificationCode())
                    .build();
        }

        for (FileEntity f : transfer.getFiles()){
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(f.getStoragePath())
                    .build();
            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            if(response.contentLength()!=f.getFileSize()){
                throw new ApiException("Size mismatch " + f.getOriginalFileName() +" Required :"+f.getFileSize() +" Found : "+response.contentLength() ,HttpStatus.FORBIDDEN);
            }
        }

        transfer.setStatus("completed");
        transfer.setExpiresAt(Instant.now().plus(30,ChronoUnit.MINUTES));
        fileTransferRepo.save(transfer);
        return TransferCompletionDTO.builder()
                .verificationCode(transfer.getVerificationCode())
                .expiresAt(transfer.getExpiresAt())
                .transferId(transferId)
                .build();
    }
}
