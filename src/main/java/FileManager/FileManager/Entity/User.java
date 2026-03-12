package FileManager.FileManager.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name="users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true,nullable = false)
    private String clerkId;

    @Column
    private String email;


    private String name;

    @Column
    @Builder.Default
    private int credits=1000;

    @Column(updatable = false)
    @Builder.Default
    private Instant trialExpiresAt=Instant.now().plus(3, ChronoUnit.DAYS);

    private boolean isDeleted;
}
