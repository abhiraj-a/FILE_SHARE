package FileManager.FileManager.Controller;

import FileManager.FileManager.DTO.DownloadFileDTO;
import FileManager.FileManager.Service.FileService;
import FileManager.FileManager.Utils.ClerkUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<?> download(@AuthenticationPrincipal ClerkUserPrincipal principal , @PathVariable UUID id) {

        // Get signed URL from Supabase
        String signedURL = fileService.download(principal, id);

        // Build the full download URL that browser will use directly
        // This URL points to Supabase
        String fullDownloadUrl = "https://nnjgyyrhidboaqvdwrwc.supabase.co/storage/v1" + signedURL+"&response-content-disposition=attachment";

        // Return URL as JSON - browser will download directly from Supabase
        Map<String, String> response = new HashMap<>();

        response.put("downloadUrl", fullDownloadUrl);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal ClerkUserPrincipal principal  , @RequestBody List<UUID> ids){
        fileService.delete(principal,ids);
        return ResponseEntity.ok().build();
    }
}