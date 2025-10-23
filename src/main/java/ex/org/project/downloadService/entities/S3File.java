package ex.org.project.downloadService.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Entity
@NoArgsConstructor
@Table(name = "s3_file", schema = "public")
public class S3File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String uuid;
    @Column(name = "s3_etag")
    private String s3Etag;
    private String fileName;
    private Integer fileTypeId;
    private String filePath;
    @Transient
    private String fileKey;
    @Transient
    private String fileBucket;
    private Boolean toBeRemoved;
    private String checksumHash;
    private String description;
    private Timestamp uploadedAt;
    private String uploadedBy;
    private Timestamp updatedAt;
    private String updatedBy;

    public S3File(String fileName, String filePath){
        this.fileName = fileName;
        this.filePath = filePath;
        this.setS3FileKeyAndBucketFromPath();
    }

    public void setS3FileKeyAndBucketFromPath() {
        if(this.filePath == null) {
            setFileKey(null);
            return;
        }
        //path form: bucket/path/to/file
        String[] parts = this.filePath.split("/", 2);
        setFileBucket(parts[0]);
        setFileKey(parts[1]);
    }

}
