package com.xl365vc.api.service.interfaces;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageInterface {

	String storeFile(MultipartFile file);

	Resource loadFileAsResource(String fileName);

	void deleteFile(String fileName);

}
