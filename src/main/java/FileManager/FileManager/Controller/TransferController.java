package FileManager.FileManager.Controller;


import FileManager.FileManager.Service.TransferService;
import FileManager.FileManager.Utils.ClerkUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;


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


    @GetMapping("/download-zip/{verificationCode}")
    public ResponseEntity<?> download(@AuthenticationPrincipal
                                           ClerkUserPrincipal principal  ,
                                          @PathVariable String verificationCode){
        return ResponseEntity.ok(transferService.downloadTransfer(principal,verificationCode));
    }


    @PutMapping("/revoke/{id}")
    public ResponseEntity<?> revoke(@AuthenticationPrincipal
                                           ClerkUserPrincipal principal  ,
                                           @PathVariable UUID code){
        transferService.revokeTransfer(principal,code);
        return ResponseEntity.ok().build();
    }





}
