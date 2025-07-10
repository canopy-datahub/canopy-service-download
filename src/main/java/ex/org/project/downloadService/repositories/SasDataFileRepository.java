package ex.org.project.downloadService.repositories;

import ex.org.project.downloadService.entities.SasDataFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SasDataFileRepository extends JpaRepository<SasDataFile, Integer> {}
