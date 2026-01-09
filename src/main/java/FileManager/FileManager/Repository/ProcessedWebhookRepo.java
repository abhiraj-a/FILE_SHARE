package FileManager.FileManager.Repository;

import FileManager.FileManager.Entity.ProcessedWebhook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedWebhookRepo  extends JpaRepository<ProcessedWebhook, UUID> {

    boolean existsBySvixId(String svixId);
}
