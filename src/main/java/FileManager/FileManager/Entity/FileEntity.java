package FileManager.FileManager.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "files")
@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id" ,nullable = false)
    private User owner;

    @Column(nullable = true , unique = true)
    private String storedFileName;

    @Column(updatable = false , nullable = false)
    private Instant createdAt;

    @Column(nullable = true)
    private String fileType;

    @Column(nullable = true)
    private String storagePath;

    @Column(nullable = false)
    private String contentType;


}
