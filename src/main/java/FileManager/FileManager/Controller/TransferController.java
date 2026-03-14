package FileManager.FileManager.Controller;
import FileManager.FileManager.DTO.TransferinitDTO;
import FileManager.FileManager.Repository.FileTransferRepo;
import FileManager.FileManager.Service.FileService;
import FileManager.FileManager.ServiceImpl.TransferServiceImpl;
import FileManager.FileManager.Utils.ClerkUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferServiceImpl transferService;

    private  final FileTransferRepo fileTransferRepo;

    private final FileService fileService;

//    @PostMapping("/files-to-transfer")
//    public ResponseEntity<?> transferFiles(@AuthenticationPrincipal
//                                           ClerkUserPrincipal principal  ,
//                                           @RequestBody List<UUID> fileIds){
//        return ResponseEntity.ok(transferService.transferFiles(principal, fileIds));
//    }

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

        return ResponseEntity.ok(transferService.downloadTransfer(principal, verificationCode));
    }



    @PutMapping("/revoke/{code}")
    public ResponseEntity<?> revoke(@AuthenticationPrincipal
                                           ClerkUserPrincipal principal  ,
                                           @PathVariable UUID code){
        transferService.revokeTransfer(principal,code);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/init-transfer")
    public ResponseEntity<?> initTransfer(@AuthenticationPrincipal
                                           ClerkUserPrincipal principal, @RequestBody List<TransferinitDTO> files){
        return ResponseEntity.ok(transferService.init(principal,files));
    }

    @PostMapping("/init-ransfer/confirm")
    public ResponseEntity<?> confirm(@AuthenticationPrincipal
    ClerkUserPrincipal principal , String transferId){
        return ResponseEntity.ok(transferService.confirm(principal,transferId));
    }





}
