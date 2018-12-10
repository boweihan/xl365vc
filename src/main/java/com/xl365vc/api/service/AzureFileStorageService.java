package com.xl365vc.api.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.xl365vc.api.entity.FileVersion;
import com.xl365vc.api.exception.FileStorageException;
import com.xl365vc.api.exception.MyFileNotFoundException;
import com.xl365vc.api.property.FileStorageProperties;
import com.xl365vc.api.service.interfaces.MultiUserFileStorageInterface;

@Service("azurefileservice")
public class AzureFileStorageService implements MultiUserFileStorageInterface {

	@Autowired
	private Environment env;

	@Autowired
	private CloudStorageAccount cloudStorageAccount;
	
	private final Path fileStorageLocation;

    @Autowired
    public AzureFileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception e) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", e);
        }
    }

    @Override
    public String storeFile(String userPrincipal, String fileId, MultipartFile file) {
    	String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		try {
	        final CloudBlobClient blobClient = cloudStorageAccount.createCloudBlobClient();
	        final CloudBlobContainer container = blobClient.getContainerReference(env.getProperty("spring.cloud.azure.storage.container"));
	        final CloudBlobDirectory userDirectory = container.getDirectoryReference(userPrincipal);
	        final CloudBlobDirectory fileDirectory = userDirectory.getDirectoryReference(fileId);
	        final CloudBlockBlob blob = fileDirectory.getBlockBlobReference(fileName);
	        blob.upload(file.getInputStream(), file.getSize());
	        return fileName;
		} catch (URISyntaxException | StorageException | IOException e) {
			throw new FileStorageException("Could not store file " + fileName + ". Please try again!", e);
		}
    }
    
    @Override
    public Resource loadFileAsResource(String userPrincipal, String fileId, String fileName) {
        try {
        	fileName = URLDecoder.decode(fileName, "UTF-8");
	        final CloudBlobClient blobClient = cloudStorageAccount.createCloudBlobClient();
	        final CloudBlobContainer container = blobClient.getContainerReference(env.getProperty("spring.cloud.azure.storage.container"));
	        final CloudBlobDirectory userDirectory = container.getDirectoryReference(userPrincipal);
	        final CloudBlobDirectory fileDirectory = userDirectory.getDirectoryReference(fileId);
	        final CloudBlockBlob blob = fileDirectory.getBlockBlobReference(fileName);
	        String tempFilePath = this.fileStorageLocation + "/" + fileName;
	        // download to temp directory
	        blob.downloadToFile(tempFilePath);
	        // pull the resource from temp directory
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (URISyntaxException | StorageException | IOException e) {
        	System.out.println(e);
            throw new MyFileNotFoundException("File not found " + fileName, e);
        }
    }
    
    @Override
    public void deleteFile(String userPrincipal, String fileId, String fileName) {
        try {
        	fileName = URLDecoder.decode(fileName, "UTF-8");
	        final CloudBlobClient blobClient = cloudStorageAccount.createCloudBlobClient();
	        final CloudBlobContainer container = blobClient.getContainerReference(env.getProperty("spring.cloud.azure.storage.container"));
	        final CloudBlobDirectory userDirectory = container.getDirectoryReference(userPrincipal);
	        final CloudBlobDirectory fileDirectory = userDirectory.getDirectoryReference(fileId);
	        final CloudBlockBlob blob = fileDirectory.getBlockBlobReference(fileName);
	        blob.delete();
        } catch (URISyntaxException | StorageException | UnsupportedEncodingException e) {
        	throw new FileStorageException("Unable to delete file", e);
        }
    }

    @Override
    public List<FileVersion> getAvailableFiles(String userPrincipal, String fileId) {
    	List<FileVersion> fileNames = new ArrayList<>();
    	
        try {
	        final CloudBlobClient blobClient = cloudStorageAccount.createCloudBlobClient();
	        final CloudBlobContainer container = blobClient.getContainerReference(env.getProperty("spring.cloud.azure.storage.container"));
	        final CloudBlobDirectory userDirectory = container.getDirectoryReference(userPrincipal);
	        final CloudBlobDirectory fileDirectory = userDirectory.getDirectoryReference(fileId);

	        for (ListBlobItem blob : fileDirectory.listBlobs()) {
	        	CloudBlockBlob blobe = (CloudBlockBlob) blob;
	        	String[] uriSegments = blob.getUri().toString().split("/");
				fileNames.add(
					new FileVersion(
						uriSegments[uriSegments.length - 1],
						blobe.getProperties().getLastModified().toInstant()
					)
				);
	        }
        } catch (URISyntaxException | StorageException e) {
        	throw new FileStorageException("Unable to get available file names", e);
        }
    	return fileNames;
    }
}
