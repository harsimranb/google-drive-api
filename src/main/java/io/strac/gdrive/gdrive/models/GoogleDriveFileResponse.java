package io.strac.gdrive.gdrive.models;

import com.google.api.services.drive.model.File;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleDriveFileResponse { ;
    private String id;
    private String name;
    private String mimeType;
    private Long size;
    private String lastModifiedDateTime;

    public GoogleDriveFileResponse(final File googleDriveFile) {
        this.id = googleDriveFile.getId();
        this.name = googleDriveFile.getName();
        this.mimeType = googleDriveFile.getMimeType();
        this.size = googleDriveFile.getSize();
        this.lastModifiedDateTime = googleDriveFile.getModifiedTime().toString();
    }
}