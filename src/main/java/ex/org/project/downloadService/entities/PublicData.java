package ex.org.project.downloadService.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "public_data", schema = "public")
public class PublicData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String fileName;

    private Long fileSize;

    private String fileCategory;

    @Column(name = "s3_file_id")
    private Integer s3FileId;

}
