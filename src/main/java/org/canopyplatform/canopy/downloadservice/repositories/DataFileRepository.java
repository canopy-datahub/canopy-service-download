package org.canopyplatform.canopy.downloadservice.repositories;

import org.canopyplatform.canopy.downloadservice.entities.DataFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataFileRepository extends JpaRepository<DataFile, Integer> {

    List<DataFile> findDataFilesByDataSubmission_Id(Integer submissionId);

    List<DataFile> findDataFilesByDataSubmission_StudyId(Integer studyId);

    List<DataFile> findDataFilesByDataSubmission_StudyIdAndFileCategoryCategoryGroup(Integer studyId, String categoryGroup);

    Optional<DataFile> findDataFileByIdAndDataSubmission_StudyId(Integer fileId, Integer studyId);

    Optional<DataFile> findDataFileByIdAndDataSubmission_StudyIdAndFileCategoryCategoryGroup(Integer fileId, Integer studyId, String categoryGroup);

    List<DataFile> findByIdIn(List<Integer> ids);

}
