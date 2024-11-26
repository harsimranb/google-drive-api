package io.strac.gdrive.gdrive.controllers;

import com.google.api.client.util.DateTime;
import io.strac.gdrive.gdrive.models.GoogleDriveFileListResponse;
import io.strac.gdrive.gdrive.models.GoogleDriveFileResponse;
import io.strac.gdrive.gdrive.services.GoogleDriveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class FilesControllerTests {

    @Mock
    private GoogleDriveService googleDriveService;

    @InjectMocks
    private FilesController filesController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(filesController).build();
    }

    @Test
    public void test_getFilesPaginated_success() throws Exception {
        final DateTime modifiedDateTime = new DateTime(System.currentTimeMillis());
        final GoogleDriveFileResponse file = new GoogleDriveFileResponse();
        file.setId("test-file-id");
        file.setLastModifiedDateTime(modifiedDateTime.toString());
        when(googleDriveService.getFilesPaginated(null)).thenReturn(new GoogleDriveFileListResponse(List.of(file), null));

        mockMvc.perform(get(FilesController.API_RESOURCE_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.files[0].id").value("test-file-id"));

        verify(googleDriveService, times(1)).getFilesPaginated(null);
    }

    @Test
    public void test_downloadFile_success() throws Exception {
        final DateTime modifiedDateTime = new DateTime(System.currentTimeMillis());
        String fileId = "test-file-id";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write("Test content".getBytes());

        GoogleDriveFileResponse file = new GoogleDriveFileResponse();
        file.setId(fileId);
        file.setName("test.txt");
        file.setMimeType("text/plain");
        file.setLastModifiedDateTime(modifiedDateTime.toString());

        when(googleDriveService.getById(fileId)).thenReturn(file);
        doAnswer(invocation -> {
            ((ByteArrayOutputStream) invocation.getArgument(1)).write("Test content".getBytes());
            return null;
        }).when(googleDriveService).downloadById(eq(fileId), any(ByteArrayOutputStream.class));

        mockMvc.perform(get(FilesController.API_RESOURCE_NAME + "/{fileId}", fileId))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test.txt"))
                .andExpect(content().contentType("text/plain"))
                .andExpect(content().bytes("Test content".getBytes()));

        verify(googleDriveService, times(1)).getById(fileId);
        verify(googleDriveService, times(1)).downloadById(eq(fileId), any(ByteArrayOutputStream.class));
    }

    @Test
    public void test_uploadFile_success() throws Exception {
        final DateTime modifiedDateTime = new DateTime(System.currentTimeMillis());
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Test content".getBytes());

        GoogleDriveFileResponse uploadedFile = new GoogleDriveFileResponse();
        uploadedFile.setId("file-id");
        uploadedFile.setLastModifiedDateTime(modifiedDateTime.toString());

        when(googleDriveService.uploadFile(any(), any())).thenReturn(uploadedFile);
        mockMvc.perform(multipart(FilesController.API_RESOURCE_NAME).file(multipartFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("file-id"));

        verify(googleDriveService, times(1)).uploadFile(any(), any());
    }

    @Test
    public void test_deleteFile_success() throws Exception {
        String fileId = "test-file-id";

        doNothing().when(googleDriveService).deleteFile(fileId);

        mockMvc.perform(delete(FilesController.API_RESOURCE_NAME + "/{fileId}", fileId))
                .andExpect(status().isOk());

        verify(googleDriveService, times(1)).deleteFile(fileId);
    }
}
