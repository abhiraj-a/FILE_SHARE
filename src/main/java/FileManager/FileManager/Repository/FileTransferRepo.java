package FileManager.FileManager.Repository;

import FileManager.FileManager.Entity.FileTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileTransferRepo extends JpaRepository<FileTransfer, UUID> {
    Optional<FileTransfer> findByVerificationCode(String verificationCode);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "DELETE FROM file_transfer_files WHERE file_id IN (:fileIds)",
            nativeQuery = true
    )
    int deleteFileReferences(@Param("fileIds") List<UUID> fileIds);



    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = """
        DELETE FROM file_transfer_files
        WHERE file_id IN (
            SELECT id FROM files WHERE owner_id = :userId
        )
        """,
            nativeQuery = true
    )
    int deleteJoinRowsByUser(@Param("userId") UUID userId);
    List<FileTransfer> findAllByFiles_Id(UUID id);
}
