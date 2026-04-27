package org.canopyplatform.canopy.downloadservice.repositories;

import org.canopyplatform.canopy.downloadservice.entities.UserFileUpload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFileUploadRepository extends JpaRepository<UserFileUpload, Integer> {}
