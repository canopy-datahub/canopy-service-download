package org.canopyplatform.canopy.downloadservice.repositories;

import org.canopyplatform.canopy.downloadservice.entities.SasDataFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SasDataFileRepository extends JpaRepository<SasDataFile, Integer> {}
