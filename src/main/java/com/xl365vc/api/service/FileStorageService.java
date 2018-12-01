package com.xl365vc.api.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.xl365vc.api.entity.FileVersion;
import com.xl365vc.api.exception.FileStorageException;
import com.xl365vc.api.exception.InvalidEntityException;
import com.xl365vc.api.exception.MyFileNotFoundException;
import com.xl365vc.api.property.FileStorageProperties;
import com.xl365vc.api.service.interfaces.FileStorageInterface;

@Service
public class FileStorageService implements FileStorageInterface {

	private final Path fileStorageLocation;
	
    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception e) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException e) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", e);
        }
    }
    
    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new MyFileNotFoundException("File not found " + fileName, e);
        }
    }
    
    @Override
    public void deleteFile(String fileName) {
    	try {
    		Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
    		File file = new File(filePath.toString());
    		if (file.delete()) {
    			System.out.println("file successfully deleted");
    		} else {
    			throw new Exception();
    		}
    	} catch (Exception e) {
    		throw new FileStorageException("Unable to delete file", e);
    	}
    }

    public List<FileVersion> getAvailableFileVersions() {
    	List<FileVersion> fileNames = new ArrayList<>();
    	try (Stream<Path> paths = Files.walk(this.fileStorageLocation)) {
    	    paths
    	        .filter(Files::isRegularFile)
    	        .forEach(p -> {
					try {
						fileNames.add(
							new FileVersion(
								URLEncoder.encode(p.getFileName().toString(), "UTF-8")
							)
						);
					} catch (UnsupportedEncodingException e) {
						throw new FileStorageException("File name unable to be utf-8 encoded", e);
					}
				});
    	}  catch (Exception e) {
    		throw new FileStorageException("Unable to get available file names", e);
    	}
    	return fileNames;
    }
}
