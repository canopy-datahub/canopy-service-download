package ex.org.project.downloadService.services;

import org.springframework.http.ResponseEntity;

import java.util.List;

public interface RetrievalService {

    ResponseEntity<Object> getSubmissionFiles(Integer submissionId);

    ResponseEntity<Object> getDocumentFile(Integer fileId, Integer studyId);

    ResponseEntity<Object> getStudyDocuments(Integer studyId);

    ResponseEntity<Object> getSelectedFiles(List<Integer> dataFiles, List<Integer> sasFiles, Integer userId);

    ResponseEntity<Object> getMetaOrDictFile(Integer fileId, Boolean downloadYaml);

    ResponseEntity<Object> getVariableReport();

    ResponseEntity<Object> getVariablesPage();

    ResponseEntity<Object> getStudyMtaForm(Integer studyId);

    ResponseEntity<Object> getUuidSpreadsheet();

    ResponseEntity<Object> getPublicData(List<Integer> fileIds);

    ResponseEntity<Object> getUploadPortalFile(Integer uploadId, Integer userId);
}
