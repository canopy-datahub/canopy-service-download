package org.canopyplatform.canopy.downloadservice.models;

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
