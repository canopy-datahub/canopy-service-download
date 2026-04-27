package org.canopyplatform.canopy.downloadService.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sas_file_download", schema = "public")
public class SasFileDownload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer sasFileId;

    private Integer downloadBy;

    private Timestamp downloadAt;

    public SasFileDownload(Integer sasFileId, Integer downloadBy){
        this.sasFileId = sasFileId;
        this.downloadBy = downloadBy;
        this.downloadAt = Timestamp.from(Instant.now());
    }
}
