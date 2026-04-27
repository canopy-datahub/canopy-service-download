package org.canopyplatform.canopy.downloadservice.services;

import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DownloadService {

	ResponseEntity<Object> downloadFile(Integer fileId);

	ResponseEntity<Object> downloadFile(String fileName, String filePath);

	ResponseEntity<Object> downloadFiles(List<Integer> fileIds, String name);

	ResponseEntity<Object> initiateYamlDownload(Integer s3FileId);

}
