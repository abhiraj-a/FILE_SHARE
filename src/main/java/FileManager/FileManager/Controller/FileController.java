package FileManager.FileManager.Controller;

import FileManager.FileManager.Service.FileService;
import FileManager.FileManager.Utils.ClerkUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/get-All-Files")
    public ResponseEntity<?> getAllFiles(@AuthenticationPrincipal ClerkUserPrincipal principal){
        return ResponseEntity.ok(fileService.getAllFiles(principal));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@AuthenticationPrincipal ClerkUserPrincipal principal , @RequestPart("files") List<MultipartFile> files){
        return ResponseEntity.ok(fileService.uploadFiles(principal, files));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> download(@AuthenticationPrincipal ClerkUserPrincipal principal , @PathVariable UUID id){
        return ResponseEntity.ok(fileService.download(principal, id));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal ClerkUserPrincipal principal  , @RequestBody List<UUID> ids){
        fileService.delete(principal,ids);
        return ResponseEntity.ok().build();
    }

}
