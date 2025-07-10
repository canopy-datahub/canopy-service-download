package ex.org.project.downloadService.repositories;

import ex.org.project.downloadService.entities.Study;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<Study, Integer> {}
