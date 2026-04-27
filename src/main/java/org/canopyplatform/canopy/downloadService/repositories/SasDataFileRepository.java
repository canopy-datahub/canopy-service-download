package org.canopyplatform.canopy.downloadService.repositories;

import org.canopyplatform.canopy.downloadService.entities.SasDataFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SasDataFileRepository extends JpaRepository<SasDataFile, Integer> {}
