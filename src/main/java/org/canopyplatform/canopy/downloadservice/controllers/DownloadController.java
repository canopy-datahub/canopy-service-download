package ex.org.project.downloadService.controllers;

import org.canopyplatform.canopy.downloadservice.auth.AccessRole;
import org.canopyplatform.canopy.downloadservice.auth.core.KeycloakAuthenticationService;
import org.canopyplatform.canopy.downloadservice.services.RetrievalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@RestController
@RequestMapping("/download")
public class DownloadController {

    private final RetrievalService retrievalService;
    private final KeycloakAuthenticationService authenticationService;

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
    public ResponseEntity<Object> getUuidSpreadsheet(@AuthenticationPrincipal Jwt jwt){
        authenticationService.checkAuth(jwt, List.of(AccessRole.DATA_SUBMITTER));
        return retrievalService.getUuidSpreadsheet();
    }

    @RequestMapping(value = "/uploadPortal/file", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkUploadPortalFile(@AuthenticationPrincipal Jwt jwt,
                                                      @RequestParam Integer uploadId){
        authenticationService.checkAuth(jwt, List.of(AccessRole.DATA_CURATOR));
        return retrievalService.checkUploadPortalFile(uploadId)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/uploadPortal/file")
    public ResponseEntity<Object> getUploadPortalFile(@AuthenticationPrincipal Jwt jwt,
                                                      @RequestParam Integer uploadId){
        Integer userId = authenticationService.checkAuth(jwt, List.of(AccessRole.DATA_CURATOR));
        return retrievalService.getUploadPortalFile(uploadId, userId);
    }

}
