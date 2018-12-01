package com.xl365vc.api.resources;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkbookResource {
	
	@GetMapping("/workbooks")
	public String listWorkbooks() {
		return "string";
	}
	
	@PostMapping("/workbooks")
	public void createWorkbook() {
		
	}

}
