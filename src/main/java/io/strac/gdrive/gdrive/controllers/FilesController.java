package io.strac.gdrive.gdrive.controllers;

import io.strac.gdrive.gdrive.models.GoogleDriveFileResponse;
import io.strac.gdrive.gdrive.models.GoogleDriveFileListResponse;
import io.strac.gdrive.gdrive.services.GoogleDriveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;

@RestController
public class FilesController {
    public static final String API_RESOURCE_NAME = "/files";

    private static final Logger log = LoggerFactory.getLogger(FilesController.class);
    private final GoogleDriveService googleDriveService;

    @Autowired
    public FilesController(GoogleDriveService googleDriveService) {
        this.googleDriveService = googleDriveService;
    }

    /**
     * @implNote Expand to support folders, pagination, querying in the future.
     * @return HTTP Response Entity containing Google Drive files for current user
     */
    @GetMapping(API_RESOURCE_NAME)
    ResponseEntity<GoogleDriveFileListResponse> getFilesPaginated(
            @RequestParam(required = false) String nextPageToken
    ) {
        try {
            return new ResponseEntity<>(googleDriveService.getFilesPaginated(nextPageToken), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error Getting all files", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Download File from Google Drive
     * @param fileId Google Drive FileID to download
     * @return Response Stream to download a file
     */
    @GetMapping(API_RESOURCE_NAME + "/{fileId}")
    ResponseEntity<byte[]> downloadFile(@PathVariable String fileId) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            googleDriveService.downloadById(fileId, outputStream);
            final GoogleDriveFileResponse file = googleDriveService.getById(fileId);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
            headers.add(HttpHeaders.CONTENT_TYPE, file.getMimeType());

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error Downloading FileId={}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Upload a file to Google Drive
     * @return Uploaded file response
     */
    @PostMapping(path = API_RESOURCE_NAME, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<GoogleDriveFileResponse> uploadFile(@RequestParam("file") MultipartFile multipartFile) {
        try {
            // Convert MultipartFile to a File
            java.io.File file = new java.io.File(System.getProperty("java.io.tmpdir") + "/" + multipartFile.getOriginalFilename());
            multipartFile.transferTo(file);

            // Get MIME type
            String mimeType = multipartFile.getContentType();
            GoogleDriveFileResponse uploadedFile = googleDriveService.uploadFile(file, mimeType);

            return new ResponseEntity<>(uploadedFile, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error Uploading File", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete a file from GoogleDrive
     * @param fileId ID of the file to delete
     */
    @DeleteMapping(API_RESOURCE_NAME + "/{fileId}")
    ResponseEntity<String> deleteFile(@PathVariable String fileId) {
        try {
            googleDriveService.deleteFile(fileId);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            log.error("Error Deleting FileId={}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
