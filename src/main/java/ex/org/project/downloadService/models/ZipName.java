package ex.org.project.downloadService.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ZipName {
    private String name;
    private final String defaultName = "file-download";
}
