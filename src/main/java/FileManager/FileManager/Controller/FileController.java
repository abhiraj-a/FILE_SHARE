package FileManager.FileManager.Controller;

import FileManager.FileManager.DTO.DownloadFileDTO;
import FileManager.FileManager.Entity.FileEntity;
import FileManager.FileManager.ExceptionHandler.FileNotFoundException;
import FileManager.FileManager.Repository.FileEntityRepo;
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
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final FileEntityRepo fileEntityRepo;

    @GetMapping("/get-All-Files")
    public ResponseEntity<?> getAllFiles(@AuthenticationPrincipal ClerkUserPrincipal principal) {
        return ResponseEntity.ok(fileService.getAllFiles(principal));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@AuthenticationPrincipal ClerkUserPrincipal principal, @RequestPart("files") List<MultipartFile> files) {
        return ResponseEntity.ok(fileService.uploadFiles(principal, files));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Map<String, String>> download(
            @AuthenticationPrincipal ClerkUserPrincipal principal,
            @PathVariable UUID id
    ) {
        String signedPath = fileService.download(principal, id);
        // example: /object/sign/BUCKET/uuid_file.png?token=...

        FileEntity file = fileEntityRepo.findById(id)
                .orElseThrow(FileNotFoundException::new);

        String encodedFileName =
                UriUtils.encode(file.getOriginalFileName(), StandardCharsets.UTF_8);

        String separator = signedPath.contains("?") ? "&" : "?";

        String fullDownloadUrl =
                "https://nnjgyyrhidboaqvdwrwc.supabase.co/storage/v1"
                        + signedPath
                        + separator
                        + "response-content-disposition=attachment%3B%20filename%3D%22"
                        + encodedFileName
                        + "%22"
                        + "&response-content-type=application%2Foctet-stream";

        return ResponseEntity.ok(
                Map.of("downloadUrl", fullDownloadUrl)
        );
    }


//    @GetMapping("/download/{id}")
//    public ResponseEntity<?> download(@AuthenticationPrincipal ClerkUserPrincipal principal , @PathVariable UUID id) {
//
//        String signedURL = fileService.download(principal, id);
//
//        FileEntity f =fileEntityRepo.findById(id).orElseThrow(FileNotFoundException::new);
//
//        String encodedFileName = UriUtils.encode(f.getOriginalFileName(), StandardCharsets.UTF_8);
//
//        String fullDownloadUrl =
//                "https://nnjgyyrhidboaqvdwrwc.supabase.co/storage/v1"
//                        + signedURL+"&response-content-disposition=attachment%3B%20filename%3D%22"
//                        + encodedFileName
//                        + "%22"
//                        + "&response-content-type=application%2Foctet-stream";
//
//        Map<String, String> response = new HashMap<>();
//
//        response.put("downloadUrl", fullDownloadUrl);
//
//        return ResponseEntity.ok(response);
//      }

    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal ClerkUserPrincipal principal  , @RequestBody List<UUID> ids){
        fileService.delete(principal,ids);
        return ResponseEntity.ok().build();
    }
}