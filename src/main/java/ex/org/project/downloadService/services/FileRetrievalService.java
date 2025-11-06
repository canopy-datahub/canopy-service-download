package ex.org.project.downloadService.services;

import ex.org.project.downloadService.auth.UserAuthorizationException;
import ex.org.project.downloadService.entities.*;
import ex.org.project.downloadService.exceptions.custom.*;
import ex.org.project.downloadService.entities.PublicData;
import ex.org.project.downloadService.models.ZipName;
import ex.org.project.downloadService.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileRetrievalService implements RetrievalService {

    private final DataFileRepository dataFileRepository;
    private final DataSubmissionRepository dataSubmissionRepository;
    private final DownloadService downloadService;
    private final LkupDataFileCategoryRepository fileCategoryRepository;
    private final ViewStudyRepository viewStudyRepository;
    private final StudyRepository studyRepository;
    private final PublicDataRepository publicDataRepository;
    private final SasDataFileRepository sasDataFileRepository;
    private final DownloadHistoryService downloadHistoryService;
    private final FileAuthorizationService fileAuthorizationService;
    private final UserFileUploadRepository userFileUploadRepository;
    @Value("${s3.variables-page-file-path}")
    private String variablesPageFilePath;
    @Value("${s3.variable-report-file-path}")
    private String variableReportFilePath;
    @Value("${s3.study-uuid-spreadsheet-path}")
    private String uuidSpreadsheetPath;

    /**
     * Retrieves all data files associated with a submission as a zip file
     * @param submissionId
     * @return ResponseEntity with zip file
     */
    public ResponseEntity<Object> getSubmissionFiles(Integer submissionId){

        Optional<DataSubmission> submissionOpt = dataSubmissionRepository.findById(submissionId);
        if(submissionOpt.isEmpty()){
            throw new SubmissionNotFoundException("No submission found with ID: " + submissionId);
        }
        String submissionStatus = submissionOpt.get().getStatus().getName();
        if(!submissionStatus.equals("submitted") && !submissionStatus.equals("in_review")){
            throw new UserAuthorizationException("Submission status must be \"Submitted\" or \"In Review\".");
        }
        List<DataFile> dataFiles = dataFileRepository.findDataFilesByDataSubmission_Id(submissionId);

        if(dataFiles.isEmpty()){
            String emptyListWarning = "No data files found for submission id: " + submissionId;
            log.warn(emptyListWarning);
            throw new DataFileNotFoundException(emptyListWarning);
        }
        List<Integer> s3FileIds = dataFiles.stream()
                .map(DataFile::getS3FileId)
                .filter(this::nullS3FileIdFilter)
                .toList();

        String zipName = "subm_" + submissionId;

        return downloadService.downloadFiles(s3FileIds, zipName);
    }

    /**
     * Retrieves a document by fileId and studyId
     * @param fileId document file id
     * @param studyId
     * ResponseEntity containing file
     */
    public ResponseEntity<Object> getDocumentFile(Integer fileId, Integer studyId){
        Optional<DataFile> documentOptional = dataFileRepository.findDataFileByIdAndDataSubmission_StudyIdAndFileCategoryCategoryGroup(fileId, studyId, "document");
        if(documentOptional.isEmpty()){
            String emptyListWarning = "No document file found in study " + studyId + " for file id: " + fileId;
            log.warn(emptyListWarning);
            throw new DocumentFileNotFoundException(emptyListWarning);
        }
        return downloadService.downloadFile(documentOptional.get().getS3FileId());
    }

    /**
     * Retrieves data files by id and returns them as a zip file
     * @param dataFiles List of IDs corresponding to data files
     * @param sasFiles List of IDs corresponding to SAS files
     * @return ResponseEntity with zip file
     */
    public ResponseEntity<Object> getSelectedFiles(List<Integer> dataFiles, List<Integer> sasFiles, Integer userId) {
        if(dataFiles.isEmpty() && sasFiles.isEmpty()){
            throw new BadRequestException("No files were provided");
        }
        ZipName zipName = new ZipName();
        List<Integer> s3FileIds = getS3FileIds(dataFiles, sasFiles, userId, zipName);
        checkForEmptyS3List(s3FileIds, dataFiles, sasFiles);
        if(s3FileIds.size() == 1) {
            return downloadService.downloadFile(s3FileIds.get(0));
        }
        String name = zipName.getName() != null ? zipName.getName() : zipName.getDefaultName();
        return downloadService.downloadFiles(s3FileIds, name);
    }

    private List<Integer> getS3FileIds(List<Integer> dataFiles, List<Integer> sasFiles, Integer userId, ZipName zipName) {
        List<Integer> s3DataFileIds = getDataFileS3FileIds(dataFiles, userId, zipName);
        List<Integer> s3SasFileIds = getSasFileS3FileIds(sasFiles, userId, zipName);
        return Stream.of(s3DataFileIds, s3SasFileIds)
                .flatMap(Collection::stream)
                .toList();
    }


    private List<Integer> getDataFileS3FileIds(List<Integer> dataFileIds, Integer userId, ZipName zipName) {
        if(dataFileIds.isEmpty()){
            return new ArrayList<>(0);
        }
        fileAuthorizationService.checkDataFileAuthorization(dataFileIds, userId);
        List<DataFile> dataFiles = dataFileRepository.findByIdIn(dataFileIds);
        setZipNameFromDataFile(zipName, dataFiles);
        downloadHistoryService.trackDataFileDownloads(dataFiles, userId);
        return dataFiles.stream()
                .map(DataFile::getS3FileId)
                .toList();
    }

    private void setZipNameFromDataFile(ZipName zipName, List<DataFile> dataFiles) {
        if(dataFiles.isEmpty()){
            return;
        }
        Integer studyId = dataFiles.get(0).getDataSubmission().getStudyId();
        ViewStudy study = viewStudyRepository.findByStudyId(studyId);
        zipName.setName(study.getPhs());
    }

    private List<Integer> getSasFileS3FileIds(List<Integer> sasFileIds, Integer userId, ZipName zipName) {
        if(sasFileIds.isEmpty()){
            return new ArrayList<>(0);
        }
        List<SasDataFile> sasDataFiles = sasDataFileRepository.findAllById(sasFileIds);
        fileAuthorizationService.checkSasFileAuthorization(sasDataFiles, userId);
        if(zipName.getName() == null) {
            setZipNameFromSasFiles(zipName, sasDataFiles);
        }
        downloadHistoryService.trackSasFileDownloads(sasDataFiles, userId);
        return sasDataFiles.stream()
                .map(SasDataFile::getS3FileId)
                .toList();
    }

    private void setZipNameFromSasFiles(ZipName zipName, List<SasDataFile> sasDataFiles) {
        if(sasDataFiles.isEmpty()){
            return;
        }
        Integer dataFileId = sasDataFiles.get(0).getParentDataFileId();
        Optional<DataFile> dfOpt = dataFileRepository.findById(dataFileId);
        if(dfOpt.isEmpty()){
            return;
        }
        setZipNameFromDataFile(zipName, List.of(dfOpt.get()));
    }

    private void checkForEmptyS3List(List<Integer> s3FileIds, List<Integer> dataFiles, List<Integer> sasFiles) {
        if(s3FileIds.isEmpty()){
            String emptyListWarning = "No files found for data file ids: " + dataFiles + " or sas files ids: " + sasFiles;
            log.warn(emptyListWarning);
            throw new DataFileNotFoundException(emptyListWarning);
        }
    }

    /**
     * Retrieves all documents associated with a study as a zip file
     * @param studyId
     * @return ResponseEntity with zip file
     */
    public ResponseEntity<Object> getStudyDocuments(Integer studyId){
        List<DataFile> documents = dataFileRepository.findDataFilesByDataSubmission_StudyIdAndFileCategoryCategoryGroup(studyId, "document");

        if(documents.isEmpty()){
            String emptyListWarning = "No document files found for study id: " + studyId;
            log.warn(emptyListWarning);
            throw new DocumentFileNotFoundException(emptyListWarning);
        }
        List<Integer> s3FileIds = documents.stream()
                .map(DataFile::getS3FileId)
                .filter(this::nullS3FileIdFilter)
                .toList();

        ViewStudy study = viewStudyRepository.findByStudyId(studyId);
		String phsNumber = study.getPhs();

        return downloadService.downloadFiles(s3FileIds, phsNumber);
    }

    private boolean nullS3FileIdFilter(Integer s3FileId){
        if(s3FileId == null){
            log.warn("Data file with null s3 file id");
            return false;
        }
        return true;
    }

    public ResponseEntity<Object> getVariableReport(){
        return downloadService.getVariableReportPage(variableReportFilePath, "Complete-Data-Variable-Report.xlsx");
    }

    /**
     * Retrieves a metadata file by fileId
     * @param fileId data file id
     * @return ResponseEntity containing file
     */
    public ResponseEntity<Object> getDatafile(Integer fileId, Boolean downloadYaml){
        Optional<DataFile> dataFileOptional = dataFileRepository
                .findById(fileId);
        if(dataFileOptional.isEmpty()){
            String emptyListWarning = "No data file found for file ID: " + fileId;
            log.warn(emptyListWarning);
            throw new DataFileNotFoundException(emptyListWarning);
        }
        DataFile dataFile = dataFileOptional.get();

        Optional<LkupDataFileCategory> fileCategoryOpt = fileCategoryRepository.findById(dataFile.getFileCategory().getId());
        if (fileCategoryOpt.isEmpty()){
            log.warn(String.format("Data file ID { %d } has null category", fileId));
            throw new DataFileNotFoundException("No category found for data file");
        }
        switch (fileCategoryOpt.get().getCategoryGroup()){
            case "data":
//                throw new UserAuthorizationException("Please log in to access study data.");
              break;
            case "other":
                log.warn(String.format("Data file ID { %d } needs to be properly categorized", fileId));
                throw new UserAuthorizationException("Please log in to access uncategorized data.");
            case "metadata":
                //download yaml
                if(downloadYaml){
                    return downloadService.initiateYamlDownload(dataFile.getS3FileId());
                }
                break;
            case "dictionary":
                break;
            default:
                throw new DataFileNotFoundException("Invalid category group");
        }
        //return metadata or dictionary file
        return downloadService.downloadFile(dataFile.getS3FileId());
    }

    public ResponseEntity<Object> getUuidSpreadsheet(){
        return downloadService.downloadFile( "RADx-Data-Hub_Study-IDs.xlsx", uuidSpreadsheetPath);
    }

    @Transactional
    public ResponseEntity<Object> getUploadPortalFile(Integer uploadId, Integer userId){
        UserFileUpload upload = userFileUploadRepository.findById(uploadId)
                .orElseThrow(() -> new FileNotFoundException("Upload not found"));
        if(upload.getS3File() == null){
            throw new FileNotFoundException("No S3 file found for upload");
        }
        upload.setDownloadAt(Timestamp.from(Instant.now()));
        upload.setDownloadBy(userId);
        return downloadService.downloadFile(upload.getS3File());
    }

}
