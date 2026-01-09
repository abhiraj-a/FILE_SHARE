package FileManager.FileManager.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_share_link" , indexes = @Index(columnList = "tokens" , unique = true))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileShareLink {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id" , nullable = false)
    private FileEntity fileEntity;

    @Column(nullable = false , unique = true , length = 64)
    private String token;

    private Instant expiresAt;

    @Column(nullable = false)
    private boolean active=true;

    @Column(nullable = false , updatable = false)
    private Instant createdAt =Instant.now();
}
