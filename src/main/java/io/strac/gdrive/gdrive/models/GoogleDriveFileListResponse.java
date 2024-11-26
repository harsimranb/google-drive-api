package io.strac.gdrive.gdrive.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleDriveFileListResponse { ;
    private List<GoogleDriveFileResponse> files;
    private String nextPageToken;
}