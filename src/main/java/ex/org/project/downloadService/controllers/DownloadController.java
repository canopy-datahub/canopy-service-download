package ex.org.project.downloadService.controllers;

import ex.org.project.datahub.auth.core.FileAuthorizationService;
import ex.org.project.datahub.auth.core.KeycloakAuthenticationService;
import ex.org.project.datahub.auth.model.AccessRole;
import ex.org.project.downloadService.services.RetrievalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@RestController
@RequestMapping("/download")
public class DownloadController {

    private final RetrievalService retrievalService;
    private final KeycloakAuthenticationService authenticationService;
    private final FileAuthorizationService fileAuthorizationService;

    @GetMapping("/submission")
    public ResponseEntity<Object> downloadSubmission(@AuthenticationPrincipal Jwt jwt,
                                                     @RequestParam Integer submissionId){
        authenticationService.checkAuth(jwt, List.of(AccessRole.DATA_CURATOR));
        return retrievalService.getSubmissionFiles(submissionId);
    }

    @GetMapping("/study-documents")
    public ResponseEntity<Object> downloadStudyDocuments(@RequestParam Integer studyId) {
        return retrievalService.getStudyDocuments(studyId);
    }

    @GetMapping("/selected-files")
    public ResponseEntity<Object> downloadSelectedFilesTemp(@AuthenticationPrincipal Jwt jwt,
                                                            @RequestParam List<Integer> dataFiles,
                                                            @RequestParam List<Integer> sasFiles){
        Integer userId = authenticationService.checkAuth(jwt);
        return retrievalService.getSelectedFiles(dataFiles, sasFiles, userId);
    }

    @GetMapping("/meta-dict")
    public ResponseEntity<Object> downloadDataFile(@RequestParam Integer fileId, @RequestParam Optional<Boolean> yaml){
        if(yaml.isPresent()){
            return retrievalService.getMetaOrDictFile(fileId,yaml.get());
        }else{
            return retrievalService.getMetaOrDictFile(fileId,false);
        }
    }

    @GetMapping("/document")
    public ResponseEntity<Object> downloadDocument(@RequestParam Integer fileId, @RequestParam Integer studyId){
        return retrievalService.getDocumentFile(fileId, studyId);
    }

    @GetMapping("/variable-report")
    public ResponseEntity<Object> downloadVariableReport(){
        return retrievalService.getVariableReport();
    }

    @GetMapping("/study-uuids")
    public ResponseEntity<Object> getUuidSpreadsheet(@AuthenticationPrincipal Jwt jwt){
        Integer userId = authenticationService.checkAuth(jwt);
        authenticationService.checkAuth(jwt, List.of(AccessRole.DATA_SUBMITTER));
        return retrievalService.getUuidSpreadsheet();
    }

    @GetMapping("/uploadPortal/file")
    public ResponseEntity<Object> getUploadPortalFile(@AuthenticationPrincipal Jwt jwt,
                                                      @RequestParam Integer uploadId){
        Integer userId = authenticationService.checkAuth(jwt, List.of(AccessRole.DATA_CURATOR));
        return retrievalService.getUploadPortalFile(uploadId, userId);
    }

}
