package com.xl365vc.api.entity;

import java.time.Instant;

public class FileVersion {

	private String name;
	private Instant lastModified;
	
	public FileVersion(String name, Instant lastModified) {
		this.setName(name);
		this.lastModified = lastModified;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Instant getLastModified() {
		return lastModified;
	}

	public void setLastModified(Instant lastModified) {
		this.lastModified = lastModified;
	}
}
