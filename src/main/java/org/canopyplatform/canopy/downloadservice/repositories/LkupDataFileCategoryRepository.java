package org.canopyplatform.canopy.downloadservice.repositories;

import org.canopyplatform.canopy.downloadservice.entities.LkupDataFileCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LkupDataFileCategoryRepository extends JpaRepository<LkupDataFileCategory, Integer> {
}
