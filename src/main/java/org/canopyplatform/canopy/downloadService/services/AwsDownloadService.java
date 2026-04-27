package org.canopyplatform.canopy.downloadService.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.canopyplatform.canopy.downloadService.exceptions.custom.DownloadServiceReadWriteError;
import org.canopyplatform.canopy.downloadService.exceptions.custom.DownloadServiceS3FileError;
import org.canopyplatform.canopy.downloadService.models.Download;
import org.canopyplatform.canopy.downloadService.entities.S3File;
import org.canopyplatform.canopy.downloadService.models.ZipInfo;
import org.canopyplatform.canopy.downloadService.repositories.S3FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.reader.JsonArtifactReader;
import org.metadatacenter.artifacts.model.tools.YamlSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest;
import software.amazon.awssdk.transfer.s3.model.FileDownload;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class AwsDownloadService implements DownloadService {

	private final S3TransferManager transferManager;
	private final S3FileRepository fileRepository;
	private final String workingDirectory;
    private final S3AsyncClient client;
    public AwsDownloadService(S3TransferManager transferManager,
                              S3FileRepository fileRepository,
                              @Value("${s3.download-directory}") String workingDirectory,
                              S3AsyncClient client
    ){
        this.transferManager = transferManager;
        this.fileRepository = fileRepository;
        this.workingDirectory = workingDirectory;
        this.client = client;
        File dir = new File(workingDirectory);
        if(!dir.exists()){
            dir.mkdirs();
        }
    }

    public ResponseEntity<Object> downloadFile(Integer s3FileId){
        if(s3FileId == null){
            String errorMessage = "No s3 file found for requested document";
            log.warn(errorMessage);
            throw new DownloadServiceS3FileError(errorMessage);
        }

        S3File s3File = fileRepository.findById(s3FileId).orElseThrow(() -> {
            log.warn("No file found with id " + s3FileId);
            return new DownloadServiceS3FileError("No file found with id " + s3FileId);
        });

        s3File.setS3FileKeyAndBucketFromPath();
        return downloadS3File(s3File);
    }

    public ResponseEntity<Object> downloadFile(String fileName, String filePath) {
        S3File s3File = new S3File(fileName, filePath);
        return downloadS3File(s3File);
    }

    private ResponseEntity<Object> downloadS3File(S3File s3File){
        verifyValidS3File(s3File);
        File tempFile = initiateS3Download(s3File);
        return openFileStreamResponse(tempFile, s3File.getFileName());
    }

    private void verifyValidS3File(S3File s3File) {
        if(s3File.getFileKey() == null || s3File.getFileBucket() == null ||
                s3File.getFileKey().isEmpty() || s3File.getFileBucket().isEmpty()){
            String errorMessage = "Couldn't find S3 file key/bucket for retrieval";
            log.error(errorMessage);
            throw new DownloadServiceS3FileError(errorMessage);
        }
    }

    private File initiateS3Download(S3File s3File) {
        File tempFile = new File(workingDirectory + s3File.getFileName());
        DownloadFileRequest downloadFileRequest =
                DownloadFileRequest.builder()
                        .getObjectRequest(b -> b.bucket(s3File.getFileBucket()).key(s3File.getFileKey()))
                        .destination(Paths.get(tempFile.getPath()))
                        .build();
        FileDownload downloadFile = transferManager.downloadFile(downloadFileRequest);
        joinFileDownloadFuture(downloadFile, s3File.getFileName());
        return tempFile;
    }

    private void joinFileDownloadFuture(FileDownload downloadFile, String fileName) {
        try {
            downloadFile.completionFuture().join();
        } catch(CancellationException | CompletionException e) {
            String errorMessage = String.format("Error retrieving file from S3: %s", fileName);
            log.error(errorMessage, e);
            throw new DownloadServiceS3FileError(errorMessage);
        }
    }

    private ResponseEntity<Object> openFileStreamResponse(File tempFile, String fileName) {
        try {
            FileInputStream fileStream = new FileInputStream(tempFile);
            byte[] responseFile = fileStream.readAllBytes();
            fileStream.close();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + "\"" + fileName + "\"");
            ResponseEntity<Object> response = ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(responseFile);
            tempFile.delete();
            return response;
        } catch (IOException e){
            log.error("Error writing file to Response Entity", e);
            tempFile.delete();
            throw new DownloadServiceReadWriteError("Error reading file");
        }
    }
	public ResponseEntity<Object> downloadFiles(List<Integer> s3FileIds, String name) {
        verifyIdListNotEmpty(s3FileIds);

        ZipInfo zipInfo = new ZipInfo(workingDirectory + name);
		File zipDir = createDirectory(zipInfo.getName());
        zipInfo.setDirectory(zipDir);

		List<S3File> s3Files = fileRepository.findByIdIn(s3FileIds);
		List<Download> downloads = createDownloadsFromS3Files(s3Files);
        initiateFileTransferFromS3(downloads, zipInfo.getName());
        joinSuccessfulDownloadFutures(downloads);

        Path zipPath = Paths.get(zipInfo.getFileName());
        zipInfo.setPath(zipPath);
        zipInfo.setContentBuilder(new StringBuilder());
        zipInfo.getContentBuilder()
                .append(zipInfo.getFileName().split(workingDirectory)[1]).append(": {");

        zipDownloadedFiles(downloads, zipInfo);
        return openZipStreamResponse(zipInfo);
    }

    private void verifyIdListNotEmpty(List<Integer> s3FileIds) {
        if (s3FileIds.isEmpty()) {
            String errorMessage = "No S3 file id's present for selected files";
            log.error(errorMessage);
            throw new DownloadServiceS3FileError(errorMessage);
        }
    }

    private File createDirectory(String name) {
        File zipDir = new File(name);
        if (!zipDir.exists()) {
            zipDir.mkdirs();
        } else {
            try {
                FileUtils.deleteDirectory(zipDir);
                zipDir.mkdirs();
            } catch(IOException e) {
                throw new DownloadServiceReadWriteError("Could not create zip file");
            }
        }
        return zipDir;
    }

    private List<Download> createDownloadsFromS3Files(List<S3File> s3Files) {
        List<Download> downloads = new ArrayList<>();
        for (S3File s3File : s3Files) {
            s3File.setS3FileKeyAndBucketFromPath();
            if (s3File.getFileKey().isEmpty() || s3File.getFileBucket().isEmpty()) {
                String errorMessage = "Couldn't find S3 file key for retrieval: ID="
                        + s3File.getId() + ", Path=" + s3File.getFilePath();
                log.error(errorMessage);
                throw new DownloadServiceS3FileError(errorMessage);
            }
            downloads.add(new Download(s3File.getFileName(), s3File.getFileKey(), s3File.getFileBucket()));
        }
        return downloads;
    }

    private void initiateFileTransferFromS3(List<Download> downloads, String zipName) {
        for(Download download : downloads){
            download.setLocalFile(new File(zipName + "/" + download.getFileName()));
            DownloadFileRequest request =
                    DownloadFileRequest.builder()
                            .getObjectRequest(b -> b.bucket(download.getS3Bucket()).key(download.getS3Key()))
                            .destination(Paths.get(download.getLocalFile().getPath()))
                            .build();
            download.setFileDownload(transferManager.downloadFile(request));
        }
    }

    private void joinSuccessfulDownloadFutures(List<Download> downloads) {
        List<Download> downloadErrors = new ArrayList<>();
        for(Download download : downloads){
            try {
                download.getFileDownload().completionFuture().join();
            } catch(CancellationException | CompletionException e) {
                log.error(String.format("Error retrieving file from S3: %s", download.getFileName()));
                downloadErrors.add(download);
            }
        }
        downloads.removeAll(downloadErrors);
    }

    private void zipDownloadedFiles(List<Download> downloads, ZipInfo zipInfo) {
        try(FileOutputStream fos = new FileOutputStream(zipInfo.getFileName());
            ZipOutputStream zos = new ZipOutputStream(fos)) {
            for(Download download : downloads){
                ZipEntry zipEntry = new ZipEntry(download.getFileName());
                zipInfo.getContentBuilder().append(download.getFileName()).append(", ");
                zos.putNextEntry(zipEntry);
                FileInputStream fis = new FileInputStream(download.getLocalFile().getPath());
                zos.write(fis.readAllBytes());
                zos.closeEntry();
                fis.close();
            }
            zipInfo.getContentBuilder()
                    .delete(zipInfo.getContentBuilder().length() - 2, zipInfo.getContentBuilder().length())
                    .append("}");
        } catch(IOException e){
            log.error("Error writing files to zip file", e);
            cleanWorkingDirectory(zipInfo);
            throw new DownloadServiceReadWriteError("Problem creating zip file, please try again");
        }
    }

    private ResponseEntity<Object> openZipStreamResponse(ZipInfo zipInfo) {
        try {
            ResponseEntity<Object> response = createResponse(zipInfo);
            cleanWorkingDirectory(zipInfo);
            log.info(zipInfo.getContentBuilder().toString());
            return response;
        } catch (IOException e){
            log.error("Error writing files to Response Entity", e);
            cleanWorkingDirectory(zipInfo);
            throw new DownloadServiceReadWriteError("Error reading file");
        }
    }

    private ResponseEntity<Object> createResponse(ZipInfo zipInfo) throws IOException {
        FileInputStream fileStream = new FileInputStream(zipInfo.getFileName());
        byte[] responseFile = fileStream.readAllBytes();
        fileStream.close();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + zipInfo.getFileName().split(workingDirectory)[1]);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseFile);
    }

    private void cleanWorkingDirectory(ZipInfo zipInfo) {
        try {
            Files.delete(zipInfo.getPath());
            FileUtils.deleteDirectory(zipInfo.getDirectory());
        } catch (IOException deletionError){
            log.error("Error deleting zip file from working directory", deletionError);
            throw new DownloadServiceReadWriteError("Problem creating zip file; Directory deletion error");
        }
    }


    public ResponseEntity<Object> initiateYamlDownload(Integer s3FileId){
        //check if the metadata file s3 file exists
        S3File s3File = fileRepository.findById(s3FileId).orElseThrow(() -> {
            log.warn("No file found with id " + s3FileId);
            return new DownloadServiceS3FileError("No file found with id " + s3FileId);
        });
        s3File.setS3FileKeyAndBucketFromPath();
        try {
            //stream input of metadata file from S3
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3File.getFileBucket())
                    .key(s3File.getFileKey())
                    .build();
            //using s3 client to get stream of file
            var in = client.getObject(getObjectRequest, AsyncResponseTransformer.toBlockingInputStream()).get();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                //convert json to object node
                ObjectNode templateInstanceJSON = objectMapper.readValue(in, ObjectNode.class);
                JsonArtifactReader artifactReader = new JsonArtifactReader();
                TemplateInstanceArtifact templateInstance = artifactReader.readTemplateInstanceArtifact(templateInstanceJSON);

                boolean compactYaml = true;
                boolean fullQuotes = false;
                //generate yaml string from json file and write to file
                String yamlString = YamlSerializer.getYAML(templateInstance, compactYaml, fullQuotes);
                File tempFile = new File(workingDirectory + s3File.getFileName().replace(".json",".yaml"));
                try (FileWriter writer = new FileWriter(tempFile)) {
                    writer.write(yamlString);
                } catch (IOException e) {
                    log.error(String.format("ERROR: Unable to write/generate yaml file for metadata file with s3 id: %d; %s", s3FileId, e.getMessage()));
                    throw new DownloadServiceReadWriteError(String.format("ERROR: Unable to write/generate yaml for s3 file: %d; %s", s3FileId, e.getMessage()));
                }
                return openFileStreamResponse(tempFile,tempFile.getName());

            } catch (IOException e) {
                log.error(String.format("Error reading s3 file %d, %s ",s3FileId, e.getMessage()));
                throw new DownloadServiceReadWriteError(String.format("Error reading s3 file %d, %s ",s3FileId, e.getMessage()));
            }
        } catch (CancellationException | InterruptedException | ExecutionException e){
            log.error(String.format("Error occurred while attempting to access S3 file: %d, %s ", s3FileId, e.getMessage()));
            throw new RuntimeException(String.format("Error occurred while attempting to access S3 file: %d, %s ", s3FileId, e.getMessage()));
        }
    }

}
