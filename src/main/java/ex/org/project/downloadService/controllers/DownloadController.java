package ex.org.project.downloadService.controllers;

import ex.org.project.downloadService.auth.AccessRole;
import ex.org.project.downloadService.auth.UserAuthService;
import ex.org.project.downloadService.services.RetrievalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@RestController
@RequestMapping("/download")
public class DownloadController {

    private final RetrievalService retrievalService;
    private final UserAuthService authService;

    @GetMapping("/study-documents")
    public ResponseEntity<Object> downloadStudyDocuments(@RequestParam Integer studyId) {
        return retrievalService.getStudyDocuments(studyId);
    }

    @GetMapping("/selected-files")
    public ResponseEntity<Object> downloadSelectedFilesTemp(@RequestParam(value="sessionId", required = false) String sessionId,
                                                            @RequestParam List<Integer> dataFiles,
                                                            @RequestParam List<Integer> sasFiles){
        Integer userId = authService.checkAuth(sessionId);
        return retrievalService.getSelectedFiles(dataFiles, sasFiles, userId);
    }

    @GetMapping("/datafile")
    public ResponseEntity<Object> downloadDataFile(@RequestParam Integer fileId, @RequestParam Optional<Boolean> yaml){
        if(yaml.isPresent()){
            return retrievalService.getDatafile(fileId,yaml.get());
        }else{
            return retrievalService.getDatafile(fileId,false);
        }
    }

    @GetMapping("/document")
    public ResponseEntity<Object> downloadDocument(@RequestParam Integer fileId, @RequestParam Integer studyId){
        return retrievalService.getDocumentFile(fileId, studyId);
    }

    @GetMapping("/study-uuids")
    public ResponseEntity<Object> getUuidSpreadsheet(@RequestParam(value="sessionId", required = false) String sessionId){
        authService.checkAuth(sessionId, List.of(AccessRole.DATA_SUBMITTER));
        return retrievalService.getUuidSpreadsheet();
    }

    @GetMapping("/uploadPortal/file")
    public ResponseEntity<Object> getUploadPortalFile(@RequestParam String sessionId,
                                                      @RequestParam Integer uploadId){
        Integer userId = authService.checkAuth(sessionId, List.of(AccessRole.DATA_CURATOR));
        return retrievalService.getUploadPortalFile(uploadId, userId);
    }

}
