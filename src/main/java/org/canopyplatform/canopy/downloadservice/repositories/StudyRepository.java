package org.canopyplatform.canopy.downloadservice.repositories;

import org.canopyplatform.canopy.downloadservice.entities.Study;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<Study, Integer> {}
