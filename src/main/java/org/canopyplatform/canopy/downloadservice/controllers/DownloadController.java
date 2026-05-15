package org.canopyplatform.canopy.downloadservice.controllers;

import org.canopyplatform.canopy.downloadservice.auth.core.KeycloakAuthenticationService;
import org.canopyplatform.canopy.downloadservice.services.RetrievalService;
import org.canopyplatform.canopy.downloadservice.services.StudyAccessService;
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
    private final StudyAccessService studyAccessService;

    @GetMapping("/study-documents")
    public ResponseEntity<Object> downloadStudyDocuments(@AuthenticationPrincipal Jwt jwt,
                                                        @RequestParam Integer studyId) {
        studyAccessService.requireRead(jwt, studyId);
        return retrievalService.getStudyDocuments(studyId);
    }

    @GetMapping("/selected-files")
    public ResponseEntity<Object> downloadSelectedFilesTemp(@AuthenticationPrincipal Jwt jwt,
                                                            @RequestParam List<Integer> dataFiles,
                                                            @RequestParam List<Integer> sasFiles){
        Integer userId = authenticationService.checkAuth(jwt);
        // Bundled download: every requested file's parent study must be
        // readable. If any one is not, reject the whole batch — there is no
        // graceful subset to fall back to.
        for (Integer fileId : dataFiles) {
            studyAccessService.requireReadByDataFile(jwt, fileId);
        }
        for (Integer sasFileId : sasFiles) {
            studyAccessService.requireReadBySasFile(jwt, sasFileId);
        }
        return retrievalService.getSelectedFiles(dataFiles, sasFiles, userId);
    }

    @GetMapping("/datafile")
    public ResponseEntity<Object> downloadDataFile(@AuthenticationPrincipal Jwt jwt,
                                                   @RequestParam Integer fileId,
                                                   @RequestParam Optional<Boolean> yaml){
        studyAccessService.requireReadByDataFile(jwt, fileId);
        if(yaml.isPresent()){
            return retrievalService.getDatafile(fileId,yaml.get());
        }else{
            return retrievalService.getDatafile(fileId,false);
        }
    }

    @GetMapping("/document")
    public ResponseEntity<Object> downloadDocument(@AuthenticationPrincipal Jwt jwt,
                                                   @RequestParam Integer fileId,
                                                   @RequestParam Integer studyId){
        studyAccessService.requireRead(jwt, studyId);
        return retrievalService.getDocumentFile(fileId, studyId);
    }

    @GetMapping("/study-uuids")
    public ResponseEntity<Object> getUuidSpreadsheet(@AuthenticationPrincipal Jwt jwt){
        authenticationService.checkCapability(jwt, "study.uuids.export");
        return retrievalService.getUuidSpreadsheet();
    }

    @RequestMapping(value = "/uploadPortal/file", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkUploadPortalFile(@AuthenticationPrincipal Jwt jwt,
                                                      @RequestParam Integer uploadId){
        authenticationService.checkCapability(jwt, "upload-portal.file.download");
        return retrievalService.checkUploadPortalFile(uploadId)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/uploadPortal/file")
    public ResponseEntity<Object> getUploadPortalFile(@AuthenticationPrincipal Jwt jwt,
                                                      @RequestParam Integer uploadId){
        Integer userId = authenticationService.checkCapability(jwt, "upload-portal.file.download");
        return retrievalService.getUploadPortalFile(uploadId, userId);
    }

}
