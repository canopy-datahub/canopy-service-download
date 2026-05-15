package org.canopyplatform.canopy.downloadservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.canopyplatform.canopy.downloadservice.auth.AuthRole;
import org.canopyplatform.canopy.downloadservice.auth.AuthUser;
import org.canopyplatform.canopy.downloadservice.auth.UserAuthorizationException;
import org.canopyplatform.canopy.downloadservice.auth.core.KeycloakAuthenticationService;
import org.canopyplatform.canopy.downloadservice.entities.AccessLevel;
import org.canopyplatform.canopy.downloadservice.entities.DataFile;
import org.canopyplatform.canopy.downloadservice.entities.DataSubmission;
import org.canopyplatform.canopy.downloadservice.entities.SasDataFile;
import org.canopyplatform.canopy.downloadservice.entities.Study;
import org.canopyplatform.canopy.downloadservice.repositories.DataFileRepository;
import org.canopyplatform.canopy.downloadservice.repositories.SasDataFileRepository;
import org.canopyplatform.canopy.downloadservice.repositories.StudyRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Read-only per-study authorization for the download service. Mirrors the
 * read semantics of the submission/entity services' StudyAccessService:
 *
 * <ul>
 *   <li>PUBLIC — anyone, including anonymous.</li>
 *   <li>LIMITED — any authenticated user.</li>
 *   <li>PRIVATE — Creator OR users with the Curator/Admin role.</li>
 * </ul>
 *
 * <p>Resolves file → submission → study transparently; resolves sas-file →
 * parent data file → submission → study.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StudyAccessService {

    private static final String ROLE_CURATOR = "Data Curator";
    private static final String ROLE_ADMIN   = "Application Administrator";
    private static final Set<String> OVERRIDE_ROLES = Set.of(ROLE_CURATOR, ROLE_ADMIN);

    private final StudyRepository studyRepository;
    private final DataFileRepository dataFileRepository;
    private final SasDataFileRepository sasDataFileRepository;
    private final KeycloakAuthenticationService authenticationService;

    public boolean canRead(Jwt jwt, Integer studyId) {
        Study study = loadStudy(studyId);
        AccessLevel level = study.getAccessLevel();
        if (level == AccessLevel.PUBLIC) {
            return true;
        }
        if (jwt == null) {
            return false;
        }
        AuthUser user = authenticationService.getAuthenticatedUser(jwt);
        if (level == AccessLevel.LIMITED) {
            return true;
        }
        return isCreator(user, study) || hasOverrideRole(user);
    }

    public void requireRead(Jwt jwt, Integer studyId) {
        if (!canRead(jwt, studyId)) {
            throw new UserAuthorizationException(
                    "User does not have read access to study " + studyId);
        }
    }

    public void requireReadByDataFile(Jwt jwt, Integer dataFileId) {
        requireRead(jwt, lookupStudyIdForDataFile(dataFileId));
    }

    public void requireReadBySasFile(Jwt jwt, Integer sasFileId) {
        SasDataFile sas = sasDataFileRepository.findById(sasFileId)
                .orElseThrow(() -> new UserAuthorizationException(
                        "Sas data file " + sasFileId + " not found"));
        if (sas.getParentDataFileId() == null) {
            throw new UserAuthorizationException(
                    "Sas data file " + sasFileId + " has no parent data file");
        }
        requireReadByDataFile(jwt, sas.getParentDataFileId());
    }

    // ---- internals -------------------------------------------------------

    private Integer lookupStudyIdForDataFile(Integer dataFileId) {
        DataFile file = dataFileRepository.findById(dataFileId)
                .orElseThrow(() -> new UserAuthorizationException(
                        "Data file " + dataFileId + " not found"));
        DataSubmission sub = file.getDataSubmission();
        if (sub == null || sub.getStudyId() == null) {
            throw new UserAuthorizationException(
                    "Data file " + dataFileId + " has no parent study");
        }
        return sub.getStudyId();
    }

    private Study loadStudy(Integer studyId) {
        return studyRepository.findById(studyId)
                .orElseThrow(() -> new UserAuthorizationException(
                        "Study " + studyId + " not found"));
    }

    private boolean isCreator(AuthUser user, Study study) {
        return study.getCreatedBy() != null
                && user.getId() != null
                && study.getCreatedBy().equals(user.getId());
    }

    private boolean hasOverrideRole(AuthUser user) {
        List<AuthRole> roles = user.getRoles();
        if (roles == null) return false;
        return roles.stream()
                .map(AuthRole::getName)
                .anyMatch(OVERRIDE_ROLES::contains);
    }
}
