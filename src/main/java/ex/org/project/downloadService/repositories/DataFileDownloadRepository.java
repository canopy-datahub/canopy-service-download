package ex.org.project.downloadService.repositories;

import ex.org.project.downloadService.entities.DataFileDownload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataFileDownloadRepository extends JpaRepository<DataFileDownload, Integer> {}
