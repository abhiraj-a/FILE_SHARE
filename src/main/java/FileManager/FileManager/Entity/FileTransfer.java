package FileManager.FileManager.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileTransfer {

    @Id
    @GeneratedValue
    private UUID id;

    private String transferId;

    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" , nullable = false)
    private User owner;

    @Column(length = 5)
    private String verificationCode;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    @ManyToMany
    @JoinTable(
            name = "file_transfer_files",
            joinColumns = @JoinColumn(name = "transfer_id"),
            inverseJoinColumns = @JoinColumn(name = "file_id")
    )
    private List<FileEntity> files;
    private Instant downloadedAt;

}
