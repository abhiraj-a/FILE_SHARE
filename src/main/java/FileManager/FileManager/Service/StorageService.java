package FileManager.FileManager.Service;

import FileManager.FileManager.DTO.FileEntityDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface StorageService {

    String upload(MultipartFile file , UUID ownerId ,String path);

    String generateSignedDownload(String path,String originalFileName);

    void delete(String path);


}
