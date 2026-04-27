package org.canopyplatform.canopy.downloadservice.repositories;

import org.canopyplatform.canopy.downloadservice.entities.ViewStudy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ViewStudyRepository extends JpaRepository<ViewStudy, Long> {

   ViewStudy findByStudyId(Integer studyId);
}
