package com.xl365vc.api.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
}
