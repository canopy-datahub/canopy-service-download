package org.canopyplatform.canopy.downloadService.entities;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "data_file_download", schema = "public")
public class DataFileDownload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer dataFileId;

    private Integer downloadBy;

    private Timestamp downloadAt;

    public DataFileDownload(Integer dataFileId, Integer downloadBy){
        this.dataFileId = dataFileId;
        this.downloadBy = downloadBy;
        this.downloadAt = Timestamp.from(Instant.now());
    }

}
