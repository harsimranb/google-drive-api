package io.strac.gdrive.gdrive.services;

import com.google.api.client.http.FileContent;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import io.strac.gdrive.gdrive.clients.GoogleDriveClientFactory;
import io.strac.gdrive.gdrive.models.GoogleDriveFileListResponse;
import io.strac.gdrive.gdrive.models.GoogleDriveFileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import static io.strac.gdrive.gdrive.services.GoogleDriveService.FILE_FIELDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoogleDriveServiceTest {

    @InjectMocks
    private GoogleDriveService googleDriveService;

    @Mock
    private GoogleDriveClientFactory mockGoogleDriveClientFactory;

    @Mock
    private Drive mockGoogleDriveClient;

    @Mock
    private Drive.Files mockFiles;

    @BeforeEach
    public void setup() {
        when(mockGoogleDriveClientFactory.createClient()).thenReturn(mockGoogleDriveClient);
    }

    @Test
    public void test_getFilesPaginated_success() throws IOException {
        final String fileId = "test-file-id";
        final DateTime modifiedDateTime = new DateTime(System.currentTimeMillis());
        FileList fileList = new FileList();
        fileList.setFiles(Collections.singletonList(new File().setId(fileId).setModifiedTime(modifiedDateTime)));

        Drive.Files.List listRequest = mock(Drive.Files.List.class);

        when(mockGoogleDriveClient.files()).thenReturn(mockFiles);
        when(mockFiles.list()).thenReturn(listRequest);
        when(listRequest.setPageSize(any())).thenReturn(listRequest);
        when(listRequest.setFields(any())).thenReturn(listRequest);
        when(listRequest.setPageToken(any())).thenReturn(listRequest);
        when(listRequest.execute()).thenReturn(fileList);

        GoogleDriveFileListResponse files = googleDriveService.getFilesPaginated(null);
        assertEquals(1, files.getFiles().size());
        assertEquals(fileId, files.getFiles().getFirst().getId());
    }

    @Test
    public void test_getById_success() throws IOException {
        final DateTime modifiedDateTime = new DateTime(System.currentTimeMillis());
        final String fileId = "test-file-id";
        File file = new File()
                .setId(fileId)
                .setModifiedTime(modifiedDateTime);

        Drive.Files.Get getRequest = mock(Drive.Files.Get.class);

        when(mockGoogleDriveClient.files()).thenReturn(mockFiles);
        when(mockFiles.get(eq(fileId))).thenReturn(getRequest);
        when(getRequest.setFields(eq(FILE_FIELDS))).thenReturn(getRequest);
        when(getRequest.execute()).thenReturn(file);

        GoogleDriveFileResponse result = googleDriveService.getById(fileId);
        assertEquals(fileId, result.getId());
    }

    @Test
    public void test_downloadById_success() throws IOException {
        String fileId = "file-id";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Drive.Files.Get getRequest = mock(Drive.Files.Get.class);
        when(mockGoogleDriveClient.files()).thenReturn(mockFiles);
        when(mockFiles.get(eq(fileId))).thenReturn(getRequest);
        when(getRequest.setFields(eq(FILE_FIELDS))).thenReturn(getRequest);

        googleDriveService.downloadById(fileId, outputStream);

        verify(getRequest).executeMediaAndDownloadTo(outputStream);
    }

    @Test
    public void test_deleteFile_success() throws IOException {
        String fileId = "file-id";

        Drive.Files.Delete deleteRequest = mock(Drive.Files.Delete.class);

        when(mockGoogleDriveClient.files()).thenReturn(mockFiles);
        when(mockFiles.delete(eq(fileId))).thenReturn(deleteRequest);

        googleDriveService.deleteFile(fileId);

        verify(deleteRequest).execute();
    }

    @Test
    public void test_uploadFile_success() throws IOException {
        final DateTime modifiedDateTime = new DateTime(System.currentTimeMillis());
        java.io.File filePath = new java.io.File("test.txt");
        String mimeType = "text/plain";
        File fileMetadata = new File()
                .setId("file-id")
                .setModifiedTime(modifiedDateTime);

        Drive.Files.Create createRequest = mock(Drive.Files.Create.class);

        when(mockGoogleDriveClient.files()).thenReturn(mockFiles);
        when(mockFiles.create(any(File.class), any(FileContent.class))).thenReturn(createRequest);
        when(createRequest.setFields(eq(FILE_FIELDS))).thenReturn(createRequest);
        when(createRequest.execute()).thenReturn(fileMetadata);

        GoogleDriveFileResponse result = googleDriveService.uploadFile(filePath, mimeType);
        assertEquals("file-id", result.getId());
    }
}
