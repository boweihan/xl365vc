package com.xl365vc.api.payload;

import java.util.List;

import com.xl365vc.api.entity.FileVersion;

public class FileVersionsResponse {
	private List<FileVersion> fileVersions;
	
	public FileVersionsResponse(List<FileVersion> fileVersions) {
		this.setFileVersions(fileVersions);
	}

	public List<FileVersion> getFileVersions() {
		return fileVersions;
	}

	public void setFileVersions(List<FileVersion> fileVersions) {
		this.fileVersions = fileVersions;
	}
}
