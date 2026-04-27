package org.canopyplatform.canopy.downloadservice.services;

import org.canopyplatform.canopy.downloadservice.auth.UserAuthService;
import org.canopyplatform.canopy.downloadservice.entities.SasDataFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileAuthorizationService {

    private final UserAuthService authService;

    public void checkSasFileAuthorization(List<SasDataFile> sasFiles, Integer userId) {
        Set<Integer> parentIds = sasFiles.stream()
                .map(SasDataFile::getParentDataFileId)
                .collect(Collectors.toSet());
        authService.checkFileAuthorization(userId, parentIds);
    }

    public void checkDataFileAuthorization(List<Integer> dataFileIds, Integer userId) {
        authService.checkFileAuthorization(userId, dataFileIds);
    }

}
