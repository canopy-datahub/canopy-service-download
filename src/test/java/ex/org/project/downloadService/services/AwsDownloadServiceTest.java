package ex.org.project.downloadService.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedFileDownload;
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest;
import software.amazon.awssdk.transfer.s3.model.FileDownload;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AwsDownloadServiceTest {

    private final S3TransferManager transferManager = mock(S3TransferManager.class);

    private final AwsDownloadService downloadService = new AwsDownloadService(transferManager, null, "src/test/resources/",null);


    @Test
    void testGetVariableReportPage() throws IOException {
        Instant i = Instant.parse("2023-12-15T00:00:00.00Z");
        CompletableFuture<CompletedFileDownload> future = CompletableFuture.supplyAsync(() -> {
            return CompletedFileDownload.builder().response(GetObjectResponse.builder().lastModified(i).build()).build();
        });

        FileDownload fileDownload = mock(FileDownload.class);
        when(transferManager.downloadFile(any(DownloadFileRequest.class))).thenReturn(fileDownload);
        when(fileDownload.completionFuture()).thenReturn(future);

        File tempFile = new File("src/test/resources/filename.txt");
        try {
            tempFile.createNewFile();

            ResponseEntity<Object> response = downloadService.getVariableReportPage("src/test/resources/filename.txt", "filename.txt");

            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
            HttpHeaders headers = response.getHeaders();
            Assertions.assertEquals("[attachment; filename=12-15-2023filename.txt]",
                    headers.get(HttpHeaders.CONTENT_DISPOSITION).toString());
        } finally {
            tempFile.delete();
        }
    }
}
