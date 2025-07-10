package ex.org.project.downloadService.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "study")
@Getter
@Setter
public class Study {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String uuid;

    private String fileName;

    @Column(name="file_url")
    private String fileUrl;

    private Timestamp createdAt;

    private Integer createdBy;

    private Timestamp modifiedAt;
}
