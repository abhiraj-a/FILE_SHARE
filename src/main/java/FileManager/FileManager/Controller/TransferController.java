package FileManager.FileManager.Controller;


import FileManager.FileManager.Entity.FileEntity;
import FileManager.FileManager.Entity.FileTransfer;
import FileManager.FileManager.Repository.FileTransferRepo;
import FileManager.FileManager.Service.FileService;
import FileManager.FileManager.Service.TransferService;
import FileManager.FileManager.Utils.ClerkUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    private  final FileTransferRepo fileTransferRepo;

    private final FileService fileService;

    @PostMapping("/files-to-transfer")
    public ResponseEntity<?> transferFiles(@AuthenticationPrincipal
                                           ClerkUserPrincipal principal  ,
                                           @RequestBody List<UUID> fileIds){
        return ResponseEntity.ok(transferService.transferFiles(principal, fileIds));
    }

    @GetMapping("/receive-via-code/{verificationCode}")
    public ResponseEntity<?> recieveByCode(@AuthenticationPrincipal
                                           ClerkUserPrincipal principal  ,
                                          @PathVariable String verificationCode){
        return ResponseEntity.ok(transferService.recieveByCode(principal,verificationCode));
    }


    @GetMapping("/download-transfer/{verificationCode}")
    public ResponseEntity<?> download(@AuthenticationPrincipal
                                           ClerkUserPrincipal principal  ,
                                          @PathVariable String verificationCode){

//        FileTransfer transfer =fileTransferRepo.findByVerificationCode(verificationCode).orElseThrow(()->new RuntimeException("Transfer not found"));
//
//        List<FileEntity> files = transfer.getFiles();
//        List<Map<String,String>> response =new ArrayList<>();
//
//        for (FileEntity f : files){
//            UUID id = f.getId();
//            // Get signed URL from Supabase
//            String signedURL = fileService.download(principal, id);
//
//            // Build the full download URL that browser will use directly
//            // This URL points to Supabase
//            String fullDownloadUrl = "https://nnjgyyrhidboaqvdwrwc.supabase.co/storage/v1" + signedURL;
//
//            // Return URL as JSON - browser will download directly from Supabase
//            response.add(Map.of("downloadUrl",fullDownloadUrl));
//        }


        return ResponseEntity.ok(transferService.downloadTransfer(principal, verificationCode));
    }


    @PutMapping("/revoke/{code}")
    public ResponseEntity<?> revoke(@AuthenticationPrincipal
                                           ClerkUserPrincipal principal  ,
                                           @PathVariable UUID code){
        transferService.revokeTransfer(principal,code);
        return ResponseEntity.ok().build();
    }





}
