package io.strac.gdrive.gdrive.services;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import io.strac.gdrive.gdrive.clients.GoogleDriveClientFactory;
import io.strac.gdrive.gdrive.models.GoogleDriveFileResponse;
import io.strac.gdrive.gdrive.models.GoogleDriveFileListResponse;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Service
public class GoogleDriveService {
    public static final String FILE_FIELDS = "id, name, mimeType, size, modifiedTime";

    private final GoogleDriveClientFactory driveClientFactory;

    private static final Logger log = LoggerFactory.getLogger(GoogleDriveService.class);
    private static final int MAX_PAGE_SIZE = 5;

    @Autowired
    public GoogleDriveService(@NonNull final GoogleDriveClientFactory driveClientFactory) {
        this.driveClientFactory = driveClientFactory;
    }

    /**
     * @implNote Add querying, pagination support.
     * @return List of Google Drive Files
     */
    public GoogleDriveFileListResponse getFilesPaginated(final String nextPageToken) throws IOException {
        FileList result = this.driveClientFactory.createClient()
                .files()
                .list()
                .setPageSize(MAX_PAGE_SIZE)
                .setFields("nextPageToken, files(%s)".formatted(FILE_FIELDS))
                .setPageToken(nextPageToken)
                .execute();
        final List<GoogleDriveFileResponse> fileModels = result.getFiles()
                .stream()
                .map(GoogleDriveFileResponse::new)
                .toList();
        return new GoogleDriveFileListResponse(fileModels, result.getNextPageToken());
    }

    /**
     * @return Returns a single Google Drive File
     */
    public GoogleDriveFileResponse getById(@NonNull String fileId) throws IOException {
        final File file = this.driveClientFactory.createClient()
                .files()
                .get(fileId)
                .setFields(FILE_FIELDS)
                .execute();
        return new GoogleDriveFileResponse(file);
    }

    /**
     * Download a Google Drive File to an OutputStream
     *
     * @implNote This does not support Google Docs/Sheets/Slides. Only user uploaded content.
     */
    public void downloadById(@NonNull String fileId, final OutputStream outputStream) throws IOException {
        log.info("Download File {}", fileId);
        this.driveClientFactory.createClient()
                .files()
                .get(fileId)
                .setFields(FILE_FIELDS)
                .executeMediaAndDownloadTo(outputStream);
    }

    /**
     * Download a Google Drive File to an OutputStream
     */
    public void deleteFile(@NonNull String fileId) throws IOException {
        log.info("Delete File {}", fileId);
        this.driveClientFactory.createClient()
                .files()
                .delete(fileId)
                .execute();
    }

    /**
     * Upload a file to Google Drive
     * @return Uploaded file metadata from Google Drive
     */
    public GoogleDriveFileResponse uploadFile(java.io.File filePath, String mimeType) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(filePath.getName());

        FileContent mediaContent = new FileContent(mimeType, filePath);
        final File file = this.driveClientFactory.createClient()
                .files()
                .create(fileMetadata, mediaContent)
                .setFields(FILE_FIELDS)
                .execute();
        return new GoogleDriveFileResponse(file);
    }
}
