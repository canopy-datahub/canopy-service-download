package ex.org.project.downloadService.repositories;

import ex.org.project.downloadService.entities.UserFileUpload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFileUploadRepository extends JpaRepository<UserFileUpload, Integer> {}
