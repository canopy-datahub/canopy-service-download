package org.canopyplatform.canopy.downloadservice.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.nio.file.Path;

@Getter
@Setter
@NoArgsConstructor
public class ZipInfo {

    private String name;
    private String fileName;
    private File directory;
    private Path path;
    private StringBuilder contentBuilder;

    public ZipInfo(String name){
        this.name = name;
        this.fileName = name + ".zip";
    }

}
