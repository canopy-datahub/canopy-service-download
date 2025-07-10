package ex.org.project.downloadService.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

@Configuration
public class AwsClientConfig {

    @Bean
    public S3TransferManager transferManager() { return S3TransferManager.create(); }

}
