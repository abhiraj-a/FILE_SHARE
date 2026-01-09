package FileManager.FileManager.Repository;

import FileManager.FileManager.Entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FileEntityRepo extends JpaRepository<FileEntity , UUID> {

    List<FileEntity> findAllByOwnerId(UUID id);

    List<FileEntity> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    @Modifying
    @Query("DELETE FROM FileEntity f WHERE f.id IN :ids")
    void deleteByIds(@Param("ids") List<UUID> ids);

    @Modifying
    @Query("DELETE FROM FileEntity f WHERE f.owner.id = :userId")
    void deleteFilesByUser(@Param("userId") UUID userId);

}
