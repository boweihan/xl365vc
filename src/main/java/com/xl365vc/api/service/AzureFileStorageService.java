package com.xl365vc.api.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.xl365vc.api.entity.FileVersion;
import com.xl365vc.api.exception.FileStorageException;
import com.xl365vc.api.exception.MyFileNotFoundException;
import com.xl365vc.api.property.FileStorageProperties;
import com.xl365vc.api.service.interfaces.FileStorageInterface;

@Service
public class AzureFileStorageService implements FileStorageInterface {

	@Autowired
	private CloudStorageAccount cloudStorageAccount;

	private final String containerName = "xl365vc";
	
    @Autowired
    public AzureFileStorageService(FileStorageProperties fileStorageProperties) {
        try {
//        	createContainerIfNotExists(containerName);
        } catch (Exception e) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", e);
        }
    }
    
    private void createContainerIfNotExists(String containerName)
            throws URISyntaxException, StorageException {
            // Create a blob client.
            final CloudBlobClient blobClient = cloudStorageAccount.createCloudBlobClient();
            // Get a reference to a container. (Name must be lower case.)
            final CloudBlobContainer container = blobClient.getContainerReference(containerName);
            // Create the container if it does not exist.
            container.createIfNotExists();
      }

    @Override
    public String storeFile(MultipartFile file) {
    	String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		try {
	        final CloudBlobClient blobClient = cloudStorageAccount.createCloudBlobClient();
	        final CloudBlobContainer container = blobClient.getContainerReference(containerName);
	        CloudBlockBlob blob = container.getBlockBlobReference(fileName);
	        blob.upload(file.getInputStream(), file.getSize());
	        return fileName;
		} catch (URISyntaxException | StorageException | IOException e) {
			throw new FileStorageException("Could not store file " + fileName + ". Please try again!", e);
		}
    }
    
    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
	        final CloudBlobClient blobClient = cloudStorageAccount.createCloudBlobClient();
	        final CloudBlobContainer container = blobClient.getContainerReference(containerName);
	        CloudBlockBlob blob = container.getBlockBlobReference(fileName);
	        String tempFilePath = String.format("%temp-%s", System.getProperty("java.io.tmpdir"), fileName);
	        blob.downloadToFile(tempFilePath);
	        new File(tempFilePath).deleteOnExit();
	        return new UrlResource(tempFilePath);
        } catch (URISyntaxException | StorageException | IOException e) {
            throw new MyFileNotFoundException("File not found " + fileName, e);
        }
    }
    
    @Override
    public void deleteFile(String fileName) {
        try {
	        final CloudBlobClient blobClient = cloudStorageAccount.createCloudBlobClient();
	        final CloudBlobContainer container = blobClient.getContainerReference(containerName);
	        CloudBlockBlob blob = container.getBlockBlobReference(fileName);
	        blob.delete();
        } catch (URISyntaxException | StorageException e) {
        	throw new FileStorageException("Unable to delete file", e);
        }
    }

    @Override
    public List<FileVersion> getAvailableFiles() {
    	List<FileVersion> fileNames = new ArrayList<>();
    	
        try {
	        final CloudBlobClient blobClient = cloudStorageAccount.createCloudBlobClient();
	        final CloudBlobContainer container = blobClient.getContainerReference(containerName);

	        for (ListBlobItem blob : container.listBlobs()) {
	        	blob.getStorageUri();
	        }
        } catch (URISyntaxException | StorageException e) {
        	throw new FileStorageException("Unable to get available file names", e);
        }
    	return fileNames;
    }
}
