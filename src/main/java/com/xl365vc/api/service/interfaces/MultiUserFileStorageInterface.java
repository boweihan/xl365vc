package com.xl365vc.api.service.interfaces;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.xl365vc.api.entity.FileVersion;

public interface MultiUserFileStorageInterface {

	List<FileVersion> getAvailableFiles(String userPrincipal, String fileId);

	String storeFile(String userPrincipal, String fileId, MultipartFile file);

	Resource loadFileAsResource(String userPrincipal, String fileId, String fileName);

	void deleteFile(String userPrincipal, String fileId, String fileName);

}
