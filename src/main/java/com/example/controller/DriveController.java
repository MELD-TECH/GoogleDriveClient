package com.example.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.model.RequestModel;
import com.example.service.DriveService;
import com.google.api.services.drive.model.File;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class DriveController {

	@Autowired
	private DriveService service;
	
	@GetMapping("/files")
	public ResponseEntity<Object> returnFiles() throws GeneralSecurityException, IOException {
		
		List<File> files = service.getFiles(null);
		
		return new ResponseEntity<Object>(files, HttpStatus.OK);
	}
	
	@GetMapping("/files/{parentId}")
	public ResponseEntity<Object> getFolderDetails(@PathVariable String parentId) throws GeneralSecurityException, IOException {
		
		List<File> files = service.getFiles(parentId);
		
		return new ResponseEntity<Object>(files, HttpStatus.OK);
	}
	
	@GetMapping("/search")
	public ResponseEntity<Object> searchFiles(@RequestBody RequestModel model) throws Exception{
		
		String folderId = service.searchForFiles(model.getParentId(), model.getFolderName());
		
		return new ResponseEntity<Object>(folderId, HttpStatus.OK);
	}
	
	@PostMapping("/upload")
	public ResponseEntity<Object> uploadImages(@RequestBody MultipartFile file, @RequestParam String filepath) throws Exception{
		
		String uploadFileId = service.uploadFile(file, filepath);
		
		return new ResponseEntity<Object>(uploadFileId, HttpStatus.OK);
	}
	
	@GetMapping("/download/{fileId}")
	public ResponseEntity<Object> downloadImages(@PathVariable String fileId) throws Exception{
		
		service.downloadFile(fileId);
		
		return new ResponseEntity<Object>("Files successfully downloaded ", HttpStatus.OK);
	}
	
	@DeleteMapping("/remove/{fileId}")
	public ResponseEntity<Object> removeImages(@PathVariable String fileId) throws Exception{
		
		service.deleteImage(fileId);
		
	return new ResponseEntity<Object>("Files successfully removed ", HttpStatus.OK);
		
	}
	
	@GetMapping("/get-image/{fileId}")
	public ResponseEntity<Object> getImage(@PathVariable String fileId) throws Exception{
		
		File file = service.readImage(fileId);
		
		return new ResponseEntity<Object>(file, HttpStatus.OK);
	}
}
