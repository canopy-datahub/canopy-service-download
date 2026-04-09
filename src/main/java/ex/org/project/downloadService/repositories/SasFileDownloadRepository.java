package ex.org.project.downloadService.repositories;

import ex.org.project.downloadService.entities.SasFileDownload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SasFileDownloadRepository extends JpaRepository<SasFileDownload, Integer> {}
