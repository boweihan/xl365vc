package com.xl365vc.api.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.xl365vc.api.entity.FileVersion;
import com.xl365vc.api.payload.FileVersionsResponse;
import com.xl365vc.api.payload.UploadFileResponse;
import com.xl365vc.api.service.FileStorageService;

@RestController
@RequestMapping("/versions")
public class FileVersionController {
	
	private static final Logger logger = LoggerFactory.getLogger(FileVersionController.class);

	@Autowired
	private FileStorageService fileStorageService;

	@GetMapping
	public FileVersionsResponse getVersions() {
		List<FileVersion> fileVersions = fileStorageService.getAvailableFileVersions();
		return new FileVersionsResponse(fileVersions);
	}

	@PostMapping
    public UploadFileResponse createVersion(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }

    @GetMapping("/{name:.+}")
    public ResponseEntity<Resource> getVersionByName(@PathVariable String name, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(name);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

	@DeleteMapping("/name/{fileName}")
	public void removeVersion(@PathVariable("fileName") String fileName) {
		fileStorageService.deleteFile(fileName);
	}
}
