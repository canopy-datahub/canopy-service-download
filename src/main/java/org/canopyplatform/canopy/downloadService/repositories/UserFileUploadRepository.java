package org.canopyplatform.canopy.downloadService.repositories;

import org.canopyplatform.canopy.downloadService.entities.UserFileUpload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFileUploadRepository extends JpaRepository<UserFileUpload, Integer> {}
