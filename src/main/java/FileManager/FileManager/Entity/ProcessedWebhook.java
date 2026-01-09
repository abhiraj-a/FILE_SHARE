package FileManager.FileManager.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "processed_webhooks" , uniqueConstraints = @UniqueConstraint(columnNames = "svixId"))
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedWebhook {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false,unique = true)
    private String svixId;

    private Instant processedAt = Instant.now();
}
