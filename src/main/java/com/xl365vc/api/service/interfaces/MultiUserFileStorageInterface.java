package com.xl365vc.api.service.interfaces;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.xl365vc.api.entity.FileVersion;

public interface MultiUserFileStorageInterface {

	List<FileVersion> getAvailableFiles(String userPrincipal);

	String storeFile(String userPrincipal, MultipartFile file);

	Resource loadFileAsResource(String userPrincipal, String fileName);

	void deleteFile(String userPrincipal, String fileName);

}
