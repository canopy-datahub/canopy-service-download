package org.canopyplatform.canopy.downloadService.repositories;

import org.canopyplatform.canopy.downloadService.entities.Study;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<Study, Integer> {}
