package org.canopyplatform.canopy.downloadservice.controllers;

import org.canopyplatform.canopy.downloadservice.auth.core.KeycloakAuthenticationService;
import org.canopyplatform.canopy.downloadservice.services.RetrievalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test for {@link DownloadController}. Uses {@code @WebMvcTest} so only Spring MVC
 * is loaded.
 *
 * <p>{@code OAuth2ResourceServerAutoConfiguration} is excluded explicitly because it
 * resolves {@code spring.security.oauth2.resourceserver.jwt.jwk-set-uri} from
 * environment variables (e.g. {@code CANOPY_KEYCLOAK_JWK_SET_URI}) that are unset
 * during unit tests. We don't need a real JWT decoder here — auth is tested separately.
 *
 * <p>Security filters are disabled via {@code addFilters = false} because these tests
 * exercise controller-level routing and request validation only; auth flows are tested
 * separately and are not in scope here.
 */
@WebMvcTest(controllers = DownloadController.class,
            excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class DownloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RetrievalService retrievalService;

    /**
     * Mocked because {@link DownloadController} requires it as a constructor dependency.
     * Endpoints exercised by these tests don't actually invoke it.
     */
    @MockBean
    private KeycloakAuthenticationService authenticationService;

    @Test
    void downloadStudyDocuments() throws Exception {
        when(retrievalService.getStudyDocuments(8))
                .thenReturn(ResponseEntity.ok("octet stream"));
        this.mockMvc.perform(
                get("/download/study-documents").queryParam("studyId", "8")
        ).andExpect(status().isOk());
    }

    @Test
    void downloadStudyDocumentsWithEmptyStudyId() throws Exception {
        this.mockMvc.perform(
                get("/download/study-documents").queryParam("studyId", "")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void downloadDocument() throws Exception {
        when(retrievalService.getDocumentFile(8, 1))
                .thenReturn(ResponseEntity.ok("octet stream"));
        this.mockMvc.perform(
                get("/download/document")
                        .queryParam("fileId", "8")
                        .queryParam("studyId", "1")
        ).andExpect(status().isOk());
    }

    @Test
    void downloadDocumentWithEmptyFileId() throws Exception {
        this.mockMvc.perform(
                get("/download/document")
                        .queryParam("fileId", "")
                        .queryParam("studyId", "1")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void downloadDocumentWithEmptyStudyId() throws Exception {
        this.mockMvc.perform(
                get("/download/document")
                        .queryParam("fileId", "8")
                        .queryParam("studyId", "")
        ).andExpect(status().isBadRequest());
    }
}
