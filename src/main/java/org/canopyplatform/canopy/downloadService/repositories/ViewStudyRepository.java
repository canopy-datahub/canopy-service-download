package org.canopyplatform.canopy.downloadService.repositories;

import org.canopyplatform.canopy.downloadService.entities.ViewStudy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ViewStudyRepository extends JpaRepository<ViewStudy, Long> {

   ViewStudy findByStudyId(Integer studyId);
}
