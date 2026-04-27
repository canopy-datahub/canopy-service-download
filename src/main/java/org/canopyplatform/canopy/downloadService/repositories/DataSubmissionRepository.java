package org.canopyplatform.canopy.downloadService.repositories;

import org.canopyplatform.canopy.downloadService.entities.DataSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DataSubmissionRepository extends JpaRepository<DataSubmission, Integer> {

    Optional<DataSubmission> findById(Integer id);

}
