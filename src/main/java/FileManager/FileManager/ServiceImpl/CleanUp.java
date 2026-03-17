package FileManager.FileManager.ServiceImpl;
import FileManager.FileManager.Entity.FileEntity;
import FileManager.FileManager.Repository.FileEntityRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CleanUp {
    private final FileEntityRepo fileEntityRepo;
    private final StorageServiceImpl storageService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanUp(){
        Instant  oneDay =  Instant.now().minus(24, ChronoUnit.HOURS);
        List<FileEntity> filesToDelete = fileEntityRepo.findAllByCreatedAtBefore(oneDay);
        if(filesToDelete.isEmpty()){
            log.warn("No files to delete");
            return;
        }
        for (var f : filesToDelete){
            try {
                storageService.delete(f.getStoragePath());
                log.debug("Deleted from bucket: {}", f.getStoragePath());
            } catch (Exception e) {
                log.error("Failed to delete from bucket: {} - {}", f.getStoragePath(), e.getMessage());
            }
        }
    }
}
