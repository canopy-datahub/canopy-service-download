package ex.org.project.downloadService.services;

import org.springframework.http.ResponseEntity;

import java.util.List;

public interface RetrievalService {

    ResponseEntity<Object> getSubmissionFiles(Integer submissionId);

    ResponseEntity<Object> getDocumentFile(Integer fileId, Integer studyId);

    ResponseEntity<Object> getStudyDocuments(Integer studyId);

    ResponseEntity<Object> getSelectedFiles(List<Integer> dataFiles, List<Integer> sasFiles, Integer userId);

    ResponseEntity<Object> getDatafile(Integer fileId, Boolean downloadYaml);

    ResponseEntity<Object> getVariableReport();

    ResponseEntity<Object> getUuidSpreadsheet();

    ResponseEntity<Object> getUploadPortalFile(Integer uploadId, Integer userId);
}
