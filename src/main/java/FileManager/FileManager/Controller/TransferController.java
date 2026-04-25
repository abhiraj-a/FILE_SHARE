package FileManager.FileManager.Controller;
import FileManager.FileManager.DTO.*;
import FileManager.FileManager.Entity.FileTransfer;
import FileManager.FileManager.ExceptionHandler.TransferNotFoundException;
import FileManager.FileManager.Repository.FileTransferRepo;
import FileManager.FileManager.Service.FileService;
import FileManager.FileManager.ServiceImpl.EmailService;
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
    private final EmailService emailService;

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
                                           @PathVariable String code){
        transferService.revokeTransfer(principal,code);
        return ResponseEntity.ok().build();
    }


    // now adding beneficiary emails to send the verification code
    @PostMapping("/init-transfer")
    public ResponseEntity<?> initTransfer(@AuthenticationPrincipal
                                           ClerkUserPrincipal principal, @RequestBody WrapperDto wrapperDto){

        List<TransferinitDTO> files = wrapperDto.getFiles();
        BeneficiaryDTO beneficiaryDTO = wrapperDto.getBeneficiaryDTO();
        InitMultiDTO init = transferService.init(principal,files);
        if(beneficiaryDTO!=null ){
            FileTransfer transfer = fileTransferRepo.findByTransferId(init.getTransferId()).orElseThrow(TransferNotFoundException::new);
            List<String> emails =beneficiaryDTO.getEmails();
            for (String email : emails){
                emailService.sendVerificationEmail(email,"",transfer.getVerificationCode());
            }
        }
        return ResponseEntity.ok(init);
    }

    @PostMapping("/init-transfer/confirm")
    public ResponseEntity<?> confirm(@AuthenticationPrincipal
    ClerkUserPrincipal principal , @RequestBody IncomingConfirmDTO confirmDTO){
        return ResponseEntity.ok(transferService.confirmCompletePart(confirmDTO.getUploadId(), confirmDTO.getS3Key(),confirmDTO.getPart(),confirmDTO.getTransferId()));
    }



    @GetMapping("/ongoing-transfers")
    public ResponseEntity<?> ongoing(@AuthenticationPrincipal
                                     ClerkUserPrincipal principal){
        return ResponseEntity.ok(transferService.ongoing(principal));
    }




}
