package com.xl365vc.api.service.interfaces;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.xl365vc.api.entity.FileVersion;

public interface SingleUserFileStorageInterface {

	String storeFile(MultipartFile file);

	Resource loadFileAsResource(String fileName);

	void deleteFile(String fileName);

	List<FileVersion> getAvailableFiles();

}
