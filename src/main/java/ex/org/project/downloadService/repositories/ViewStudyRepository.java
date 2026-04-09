package ex.org.project.downloadService.repositories;

import ex.org.project.downloadService.entities.ViewStudy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ViewStudyRepository extends JpaRepository<ViewStudy, Long> {

   ViewStudy findByStudyId(Integer studyId);
}
