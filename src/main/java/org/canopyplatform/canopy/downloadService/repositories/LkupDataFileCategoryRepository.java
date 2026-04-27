package org.canopyplatform.canopy.downloadService.repositories;

import org.canopyplatform.canopy.downloadService.entities.LkupDataFileCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LkupDataFileCategoryRepository extends JpaRepository<LkupDataFileCategory, Integer> {
}
