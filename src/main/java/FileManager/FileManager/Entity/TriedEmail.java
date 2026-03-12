package FileManager.FileManager.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Builder
public class TriedEmail {

    @Id
    @GeneratedValue
    private UUID id;

    private String emailHash;

    @Column(updatable = false)
    private Instant  firstSignupAt;

}
