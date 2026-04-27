package org.canopyplatform.canopy.downloadService.controllers;

import org.canopyplatform.canopy.downloadService.auth.UserAuthService;
import org.canopyplatform.canopy.downloadService.services.RetrievalService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.main.lazy-initialization=true", classes = {DownloadController.class})
@AutoConfigureMockMvc
class DownloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RetrievalService retrievalService;
    @MockBean
    private UserAuthService authService;

    @Test
    void downloadSubmission() throws Exception{
        String submissionId = "8";
        when(retrievalService.getSubmissionFiles(8)).thenReturn(
                ResponseEntity.ok("octet stream")
        );
        this.mockMvc.perform(
                get("/download/submission")
                        .queryParam("submissionId", submissionId)
                        // TODO: Add authentication
                        // .cookie(new Cookie("chocolateChip", "session123"))
        ).andExpect(status().isOk());
    }

    @Test
    void downloadSubmissionWithEmptySubmissionId() throws Exception{
        String submissionId = "";
        this.mockMvc.perform(
                get("/download/submission").queryParam("submissionId", submissionId)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void downloadStudyDocuments() throws Exception{
        String studyId = "8";
        when(retrievalService.getStudyDocuments(8)).thenReturn(
                ResponseEntity.ok("octet stream")
        );
        this.mockMvc.perform(
                get("/download/study-documents")
                        .queryParam("studyId", studyId)
                        // TODO: Add authentication
                        // .cookie(new Cookie("chocolateChip", "session123"))
        ).andExpect(status().isOk());
    }

    @Test
    void downloadStudyDocumentsWithEmptyStudyId() throws Exception{
        String studyId = "";
        this.mockMvc.perform(
                get("/download/study-documents")
                        .queryParam("studyId", studyId)
                        // TODO: Add authentication
                        // .cookie(new Cookie("chocolateChip", "session123"))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void downloadDocument() throws Exception{
        String fileId = "8";
        String studyId = "1";
        when(retrievalService.getDocumentFile(8, 1)).thenReturn(
                ResponseEntity.ok("octet stream")
        );
        this.mockMvc.perform(
                get("/download/document")
                        .queryParam("fileId", fileId)
                        .queryParam("studyId", studyId)
                        // TODO: Add authentication
                        // .cookie(new Cookie("chocolateChip", "session123"))
        ).andExpect(status().isOk());
    }

    @Test
    void downloadDocumentWithEmptyFileId() throws Exception{
        String fileId = "";
        String studyId = "1";
        this.mockMvc.perform(
                get("/download/document")
                        .queryParam("fileId", fileId)
                        .queryParam("studyId", studyId)
                        // TODO: Add authentication
                        // .cookie(new Cookie("chocolateChip", "session123"))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void downloadDocumentWithEmptyStudyId() throws Exception{
        String fileId = "8";
        String studyId = "";
        this.mockMvc.perform(
                get("/download/document")
                        .queryParam("fileId", fileId)
                        .queryParam("studyId", studyId)
                        // TODO: Add authentication
                        // .cookie(new Cookie("chocolateChip", "session123"))
        ).andExpect(status().isBadRequest());
    }


}
