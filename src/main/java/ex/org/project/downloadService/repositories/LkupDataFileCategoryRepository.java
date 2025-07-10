package ex.org.project.downloadService.repositories;

import ex.org.project.downloadService.entities.LkupDataFileCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LkupDataFileCategoryRepository extends JpaRepository<LkupDataFileCategory, Integer> {
}
