package ex.org.project.downloadService.services;

import ex.org.project.datahub.auth.core.FileAuthorizationService;
import ex.org.project.datahub.auth.exception.UserAuthorizationException;
import ex.org.project.downloadService.entities.*;
import ex.org.project.downloadService.exceptions.custom.DataFileNotFoundException;
import ex.org.project.downloadService.exceptions.custom.DocumentFileNotFoundException;
import ex.org.project.downloadService.exceptions.custom.SubmissionNotFoundException;
import ex.org.project.downloadService.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileRetrievalServiceTest {

    @Captor
    ArgumentCaptor<List<Integer>> s3FileIdCaptor;

    @Mock
    private DataFileRepository dataFileRepository;
    @Mock
    private DownloadService downloadService;
    @Mock
    private DataSubmissionRepository dataSubmissionRepository;
    @Mock
    private LkupDataFileCategoryRepository fileCategoryRepository;
    @Mock
    private SasDataFileRepository sasDataFileRepository;
    @Mock
    private DownloadHistoryService downloadHistoryService;
    @Mock
    private FileAuthorizationService fileAuthorizationService;

    @Mock
    private ViewStudyRepository viewStudyRepository;
    @InjectMocks
    private FileRetrievalService retrievalService;

    @Test
    void getSubmissionFilesShouldReturnResponseEntity() {
        DataFile dataFile1 = new DataFile();
        DataFile dataFile2 = new DataFile();
        LkupStatus status = new LkupStatus();
        status.setId(1);
        status.setName("submitted");
        DataSubmission submission = new DataSubmission(8, 1, status);
        dataFile1.setS3FileId(1);
        dataFile1.setDataSubmission(submission);
        dataFile2.setS3FileId(2);
        dataFile2.setDataSubmission(submission);
        List<DataFile> mockDataFileList = new ArrayList<>();
        mockDataFileList.add(dataFile1);
        mockDataFileList.add(dataFile2);
        when(dataSubmissionRepository.findById(8))
                .thenReturn(Optional.of(submission));
        when(dataFileRepository.findDataFilesByDataSubmission_Id(anyInt()))
                .thenReturn(mockDataFileList);
        when(downloadService.downloadFiles(anyList(), anyString()))
                .thenReturn(ResponseEntity.ok("octet stream of zip file"));

        ResponseEntity<?> response = retrievalService.getSubmissionFiles(8);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getSubmissionFiles_EmptyDataFileList() {
        List<DataFile> mockDataFileList = new ArrayList<>();
        LkupStatus status = new LkupStatus();
        status.setId(1);
        status.setName("submitted");
        DataSubmission submission = new DataSubmission(8, 1, status);

        when(dataSubmissionRepository.findById(8))
                .thenReturn(Optional.of(submission));
        when(dataFileRepository.findDataFilesByDataSubmission_Id(anyInt()))
                .thenReturn(mockDataFileList);

        assertThrows(DataFileNotFoundException.class,
                () -> retrievalService.getSubmissionFiles(8)
        );
    }

    @Test
    void getSubmissionFiles_InvalidStatus(){
        LkupStatus status = new LkupStatus();
        status.setId(3);
        status.setName("completed");
        DataSubmission submission = new DataSubmission(8, 1, status);

        when(dataSubmissionRepository.findById(8))
                .thenReturn(Optional.of(submission));

        assertThrows(UserAuthorizationException.class,
                () -> retrievalService.getSubmissionFiles(8)
        );
    }

    @Test
    void getSubmissionFiles_SubmissionNotFound(){
        when(dataSubmissionRepository.findById(8))
                .thenReturn(Optional.empty());

        assertThrows(SubmissionNotFoundException.class,
                () -> retrievalService.getSubmissionFiles(8)
        );
    }

    @Test
    void getStudyDocumentShouldReturnResponseEntity() {
    	DataFile document1 = new DataFile();
    	DataFile document2 = new DataFile();
    	DataSubmission dataSubm1 = new DataSubmission();
    	dataSubm1.setStudyId(3);
        document1.setDataSubmission(dataSubm1);
        document2.setDataSubmission(dataSubm1);
        document1.setS3FileId(1);
        document2.setS3FileId(2);
        List<DataFile> mockDocumentList = new ArrayList<>();
        mockDocumentList.add(document1);
        mockDocumentList.add(document2);
        when(dataFileRepository.findDataFilesByDataSubmission_StudyIdAndFileCategoryCategoryGroup(3, "document")).thenReturn(mockDocumentList);
        when(downloadService.downloadFiles(anyList(), anyString())).thenReturn(
                ResponseEntity.ok("octet stream of zip file")
        );

        ViewStudy study = new ViewStudy();
        study.setStudyId(3);
        study.setPhs("phs12345");
        when(viewStudyRepository.findByStudyId(3)).thenReturn(study);

        ResponseEntity<?> response = retrievalService.getStudyDocuments(3);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(downloadService).downloadFiles(anyList(), anyString());
    }

    @Test
    void getStudyDocuments_EmptyDocumentList() {
        List<DataFile> mockDocumentList = new ArrayList<>();
        when(dataFileRepository.findDataFilesByDataSubmission_StudyIdAndFileCategoryCategoryGroup(1, "document")).thenReturn(mockDocumentList);

        assertThrows(DocumentFileNotFoundException.class,
                () -> retrievalService.getStudyDocuments(1)
        );
    }

    @Test
    void getSelectedFiles() {
        List<Integer> sasFileIds = List.of(1);
        List<Integer> dataFileIds = List.of(2);
        LkupStatus status = new LkupStatus();
        status.setId(1);
        status.setName("approved");
        DataSubmission submission = new DataSubmission(8, 1, status);
        SasDataFile dataFile1 = new SasDataFile();
        dataFile1.setId(1);
        dataFile1.setS3FileId(3);
        dataFile1.setParentDataFileId(10);
        DataFile dataFile2 = new DataFile();
        dataFile2.setId(2);
        dataFile2.setS3FileId(4);
        dataFile2.setDataSubmission(submission);

        ViewStudy study1 = new ViewStudy();
        study1.setStudyId(1);
        study1.setPhs("test123");

        Integer userId = 1;

        when(dataFileRepository.findByIdIn(argThat(arg -> arg.containsAll(
                Collections.singleton(2))))).thenReturn(List.of(dataFile2));
        when(viewStudyRepository.findByStudyId(1))
                .thenReturn(study1);
        when(sasDataFileRepository.findAllById(anyList()))
                .thenReturn(List.of(dataFile1));
        when(downloadService.downloadFiles(argThat(arg -> arg.containsAll(List.of(3, 4))), eq(study1.getPhs()))).thenReturn(
                ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body("octet stream of zip file")
        );

        ResponseEntity<?> response = retrievalService.getSelectedFiles(dataFileIds, sasFileIds, userId);

        verify(downloadService).downloadFiles(anyList(), eq(study1.getPhs()));
        verify(downloadHistoryService, times(1)).trackDataFileDownloads(anyList(), eq(userId));
        verify(downloadHistoryService, times(1)).trackSasFileDownloads(anyList(), eq(userId));
        verify(dataFileRepository, times(0)).findById(10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(3, 4), List.of(dataFile1.getS3FileId(),dataFile2.getS3FileId()));
    }

    @Test
    void getSelectedFiles_NoDataFilesFound(){
        List<Integer> sasFileIds = List.of(1);
        List<Integer> dataFileIds = List.of(2);
        Integer userId = 1;

        when(dataFileRepository.findByIdIn(anyList()))
                .thenReturn(List.of());
        when(sasDataFileRepository.findAllById(anyList()))
                .thenReturn(List.of());

        assertThrows(DataFileNotFoundException.class, () -> retrievalService.getSelectedFiles(dataFileIds, sasFileIds, userId));
    }

    @Test
    void getSelectedFiles_OnlySasFiles(){
        List<Integer> sasFileIds = List.of(1, 2);
        List<Integer> dataFileIds = List.of();
        LkupStatus status = new LkupStatus();
        status.setId(1);
        status.setName("approved");
        DataSubmission submission = new DataSubmission(8, 1, status);
        SasDataFile dataFile1 = new SasDataFile();
        dataFile1.setId(1);
        dataFile1.setS3FileId(3);
        dataFile1.setParentDataFileId(10);
        SasDataFile dataFile2 = new SasDataFile();
        dataFile2.setId(2);
        dataFile2.setS3FileId(4);
        dataFile2.setParentDataFileId(10);

        DataFile parentFile = new DataFile();
        parentFile.setDataSubmission(submission);

        ViewStudy study1 = new ViewStudy();
        study1.setStudyId(1);
        study1.setPhs("test123");

        Integer userId = 1;

        when(viewStudyRepository.findByStudyId(1))
                .thenReturn(study1);
        when(sasDataFileRepository.findAllById(anyList()))
                .thenReturn(List.of(dataFile1, dataFile2));
        when(dataFileRepository.findById(10))
                .thenReturn(Optional.of(parentFile));
        when(viewStudyRepository.findByStudyId(1))
                .thenReturn(study1);
        when(downloadService.downloadFiles(argThat(arg -> arg.containsAll(List.of(3, 4))), eq(study1.getPhs()))).thenReturn(
                ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body("octet stream of zip file")
                                                                                                                           );

        ResponseEntity<?> response = retrievalService.getSelectedFiles(dataFileIds, sasFileIds, userId);

        verify(downloadService).downloadFiles(anyList(), eq(study1.getPhs()));
        verify(dataFileRepository, times(0)).findByIdIn(anyList());
        verify(dataFileRepository, times(1)).findById(10);
        verify(downloadHistoryService, times(1)).trackSasFileDownloads(anyList(), eq(userId));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(3, 4), List.of(dataFile1.getS3FileId(),dataFile2.getS3FileId()));
    }

    @Test
    void getDocumentFile_HappyPath(){
    	DataFile document = new DataFile();
    	DataSubmission dataSubm1 = new DataSubmission();
    	dataSubm1.setStudyId(3);
        document.setDataSubmission(dataSubm1);
        document.setId(8);
        document.setS3FileId(1);

        when(dataFileRepository.findDataFileByIdAndDataSubmission_StudyIdAndFileCategoryCategoryGroup(8, 3, "document"))
                .thenReturn(Optional.of(document));
        when(downloadService.downloadFile(1))
                .thenReturn(ResponseEntity.ok("octet stream of file"));

        ResponseEntity<?> response = retrievalService.getDocumentFile(8, 3);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(downloadService, times(1)).downloadFile(1);

    }

    @Test
    void getDocumentFile_NotFound(){
        when(dataFileRepository.findDataFileByIdAndDataSubmission_StudyIdAndFileCategoryCategoryGroup(8, 3, "document"))
                .thenReturn(Optional.empty());

        assertThrows(DocumentFileNotFoundException.class,
                () -> retrievalService.getDocumentFile(8, 3)
        );
    }

    @Test
    void getMetaOrDictFile_HappyPath() {
        DataFile dataFile = new DataFile();
        LkupDataFileCategory category = new LkupDataFileCategory(6, "File Metadata - Original", "metadata");
        dataFile.setId(1);
        dataFile.setS3FileId(10);
        dataFile.setFileCategory(category);

        when(dataFileRepository.findById(1))
                .thenReturn(Optional.of(dataFile));
        when(fileCategoryRepository.findById(6))
                .thenReturn(Optional.of(category));
        when(downloadService.downloadFile(10))
                .thenReturn(ResponseEntity.ok("octet stream of zip file".getBytes()));

        ResponseEntity<Object> response = retrievalService.getMetaOrDictFile(1,false);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getMetaOrDictFile_DataFileNotFound() {
        when(dataFileRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(DataFileNotFoundException.class, () -> retrievalService.getMetaOrDictFile(1,false));
    }

    @Test
    void getMetaOrDictFile_TriedToAccessDataFile() {
        DataFile dataFile = new DataFile();
        LkupDataFileCategory category = new LkupDataFileCategory(2, "Tabular Data - Original", "data");
        dataFile.setId(1);
        dataFile.setS3FileId(10);
        dataFile.setFileCategory(category);

        when(dataFileRepository.findById(1))
                .thenReturn(Optional.of(dataFile));
        when(fileCategoryRepository.findById(2))
                .thenReturn(Optional.of(category));

        assertThrows(UserAuthorizationException.class, () -> retrievalService.getMetaOrDictFile(1,false));
    }

    @Test
    void getMetaOrDictFile_InvalidCategory() {
        DataFile dataFile = new DataFile();
        LkupDataFileCategory category = new LkupDataFileCategory(22, "Test", "test");
        dataFile.setId(1);
        dataFile.setS3FileId(10);
        dataFile.setFileCategory(category);

        when(dataFileRepository.findById(1))
                .thenReturn(Optional.of(dataFile));
        when(fileCategoryRepository.findById(22))
                .thenReturn(Optional.of(category));

        assertThrows(DataFileNotFoundException.class, () -> retrievalService.getMetaOrDictFile(1,false));
    }

    @Test
    void getMetaOrDictFile_UncategorizedDataFile() {
        DataFile dataFile = new DataFile();
        LkupDataFileCategory category = new LkupDataFileCategory(10, "Uncategorized", "other");
        dataFile.setId(1);
        dataFile.setS3FileId(10);
        dataFile.setFileCategory(category);

        when(dataFileRepository.findById(1))
                .thenReturn(Optional.of(dataFile));
        when(fileCategoryRepository.findById(10))
                .thenReturn(Optional.of(category));

        assertThrows(UserAuthorizationException.class, () -> retrievalService.getMetaOrDictFile(1 ,false));
    }

}
