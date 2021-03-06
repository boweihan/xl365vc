package com.xl365vc.api.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
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
import com.xl365vc.api.service.interfaces.MultiUserFileStorageInterface;

@RestController
@RequestMapping("file/{fileId}/versions")
public class FileVersionController {
	
	private static final Logger logger = LoggerFactory.getLogger(FileVersionController.class);

	@Autowired
	@Qualifier("azurefileservice")
	private MultiUserFileStorageInterface fileStorageService;

	@PreAuthorize("#oauth2.hasScope('read')")
	@GetMapping
	public FileVersionsResponse getVersions(
			OAuth2Authentication authentication,
			@PathVariable("fileId") String fileId
		) {
		String userPrincipal = authentication.getPrincipal().toString();
		List<FileVersion> fileVersions = fileStorageService.getAvailableFiles(userPrincipal, fileId);
		return new FileVersionsResponse(fileVersions);
	}

	@PreAuthorize("#oauth2.hasScope('write')")
	@PostMapping
    public UploadFileResponse createVersion(
    		OAuth2Authentication authentication,
    		@PathVariable("fileId") String fileId,
    		@RequestParam("file") MultipartFile file
    	) {
		String userPrincipal = authentication.getPrincipal().toString();
        String fileName = fileStorageService.storeFile(userPrincipal, fileId, file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }

	@PreAuthorize("#oauth2.hasScope('read')")
    @GetMapping("/{name:.+}")
    public ResponseEntity<Resource> getVersionByName(
    		OAuth2Authentication authentication,
    		@PathVariable("fileId") String fileId,
    		@PathVariable("name") String name,
    		HttpServletRequest request
    	) {
		String userPrincipal = authentication.getPrincipal().toString();
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(userPrincipal, fileId, name);

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

	@PreAuthorize("#oauth2.hasScope('write')")
	@DeleteMapping("/{name:.+}")
	public void removeVersion(
			OAuth2Authentication authentication,
			@PathVariable("fileId") String fileId,
			@PathVariable("name") String fileName
		) {
		String userPrincipal = authentication.getPrincipal().toString();
		fileStorageService.deleteFile(userPrincipal, fileId, fileName);
	}
}
