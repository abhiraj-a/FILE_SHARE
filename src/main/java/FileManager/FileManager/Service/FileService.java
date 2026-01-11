package FileManager.FileManager.Service;

import FileManager.FileManager.DTO.DownloadFileDTO;
import FileManager.FileManager.DTO.FileEntityDTO;
import FileManager.FileManager.Utils.ClerkUserPrincipal;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface FileService {

    List<FileEntityDTO> getAllFiles(ClerkUserPrincipal principal);

    List<FileEntityDTO> uploadFiles(ClerkUserPrincipal principal , List<MultipartFile> multipartFiles);

    String download(ClerkUserPrincipal principal , UUID id);

    DownloadFileDTO downloadFile(ClerkUserPrincipal principal , UUID id);

    void delete(ClerkUserPrincipal principal , List<UUID> id);
}
