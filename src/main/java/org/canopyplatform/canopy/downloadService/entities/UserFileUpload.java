package org.canopyplatform.canopy.downloadService.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_file_upload", schema = "public")
public class UserFileUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "study_id")
    private Integer studyId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name="s3_file_id")
    private Integer s3File;

    @Column(name = "download_by")
    private Integer downloadBy;

    @Column(name = "download_at")
    private Timestamp downloadAt;

}
