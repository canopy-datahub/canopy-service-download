package ex.org.project.downloadService.config;


import software.amazon.awssdk.regions.Region;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

@Configuration
public class AwsClientConfig {

    @Bean
    public S3TransferManager transferManager() { 
        // Log what region is being used
        Region defaultRegion = DefaultAwsRegionProviderChain.builder().build().getRegion();
        System.out.println("AWS SDK Default Region: " + defaultRegion);
        
        return S3TransferManager.create(); 
    }

}
