package org.canopyplatform.canopy.downloadService.services;

import org.canopyplatform.canopy.downloadService.entities.DataFile;
import org.canopyplatform.canopy.downloadService.entities.DataFileDownload;
import org.canopyplatform.canopy.downloadService.entities.SasDataFile;
import org.canopyplatform.canopy.downloadService.entities.SasFileDownload;
import org.canopyplatform.canopy.downloadService.repositories.DataFileDownloadRepository;
import org.canopyplatform.canopy.downloadService.repositories.SasFileDownloadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DownloadHistoryService {

    private final DataFileDownloadRepository dataFileDownloadRepository;
    private final SasFileDownloadRepository sasFileDownloadRepository;

    public void trackDataFileDownloads(List<DataFile> dataFiles, Integer userId) {
        List<DataFileDownload> downloadHistory = dataFiles.stream()
                .map(DataFile::getId)
                .map(dataFileId -> new DataFileDownload(dataFileId, userId))
                .toList();
        dataFileDownloadRepository.saveAll(downloadHistory);
    }

    public void trackSasFileDownloads(List<SasDataFile> sasFiles, Integer userId) {
        List<SasFileDownload> downloadHistory = sasFiles.stream()
                .map(SasDataFile::getId)
                .map(sasFileId -> new SasFileDownload(sasFileId, userId))
                .toList();
        sasFileDownloadRepository.saveAll(downloadHistory);
    }

}
