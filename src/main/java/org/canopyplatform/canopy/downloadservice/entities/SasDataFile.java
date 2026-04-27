package org.canopyplatform.canopy.downloadservice.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "sas_data_file", schema = "public")
public class SasDataFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "parent_data_file_id")
    private Integer parentDataFileId;

    @Column(name = "source_file_name")
    private String sourceFileName;

    @Column(name = "s3_file_id")
    private Integer s3FileId;

}
