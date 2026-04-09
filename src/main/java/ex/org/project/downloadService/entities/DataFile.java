package ex.org.project.downloadService.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "data_file", schema = "public")
public class DataFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "submission_id", referencedColumnName = "id", insertable = false, updatable = false)
    private DataSubmission dataSubmission;

    @Column(name = "source_file_name")
    private String sourceFileName;

    @Column(name = "normalized_file_name")
    private String normalizedFileName;

    @Column(name = "is_current_version")
    private Boolean isCurrentVersion;

    @Column(name = "original_data_file_id")
    private Integer originalDataFileId;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "s3_file_id")
    private Integer s3FileId;

    @Column(name="dictionary_file_id")
    private Integer dictionaryFileId;

    @Column(name="metadata_file_id")
    private Integer metadataFileId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "file_category_id")
    private LkupDataFileCategory fileCategory;
}
