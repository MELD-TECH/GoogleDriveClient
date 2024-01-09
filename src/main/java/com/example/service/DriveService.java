package com.example.service;


import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;


@Service
public class DriveService {

	JsonFactory jsonfactory = GsonFactory.getDefaultInstance();
	List<String> scopes = Collections.singletonList(DriveScopes.DRIVE);
	String tokensDirectory = "tokens";
	
	public Drive getInstance() throws IOException, GeneralSecurityException{
		NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
		Drive drive = new Drive.Builder(transport, jsonfactory, getCredentials(transport))
				.setApplicationName("First Project Demo")
				.build();
		
		return drive;
	}
	
	private Credential getCredentials(NetHttpTransport http) throws IOException {
		InputStream input = DriveService.class.getResourceAsStream("/credentials.json");
		
		GoogleClientSecrets secrets = GoogleClientSecrets.load(jsonfactory, new InputStreamReader(input));
		
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(http, jsonfactory, secrets,scopes)				
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokensDirectory)))				
				.setAccessType("offline")
				.build();
		
		LocalServerReceiver server = new LocalServerReceiver.Builder()
				.setHost("localhost")
				.setPort(8090)				
				.build();
		
		return new AuthorizationCodeInstalledApp(flow, server).authorize("user");
	}
	
	public List<File> getFiles(String parentId) throws GeneralSecurityException, IOException{
		
		
		  if(parentId == null){
			     parentId = "root";
			  }
		
		String query = "'" + parentId + "' in parents";

		FileList result = getInstance().files().list()
				.setQ(query)
				.setPageSize(10)				
				.setFields("nextPageToken, files(id, name, owners)")				
				.execute();
		
		return result.getFiles();
	}
	
	public String searchForFiles(String parentId, String folderName) throws Exception {
		
		String folderId = null;
		String pageToken = null;
				
  
		 do{			 
				String query = "mimeType = 'application/vnd.google-apps.folder'";
				
				if (parentId == null) {
			        query = query + " and 'root' in parents";
			     } else {
			        query = query + " and '" + parentId + "' in parents";
			     }
				
			FileList result = getInstance().files().list()
					.setQ(query)
					.setFields("nextPageToken, files(id, name, owners)")
					.setPageToken(pageToken)
					.execute();
			
			for(File file: result.getFiles()) {
				if(file.getName().equalsIgnoreCase(folderName)) {
					folderId = file.getId();
				}
				
				pageToken = result.getNextPageToken();
			}
		}while(pageToken != null & folderId == null);
		 
		 return folderId;
	}
	
	public String uploadFile(MultipartFile file, String filepath) throws Exception {
		
	    String folderId = getFolderId(filepath);
	    	    
	    if(folderId != null) {
	    	File filedata = new File();
		    filedata.setParents(Collections.singletonList(folderId));
		    filedata.setName(file.getOriginalFilename());
		    
			File fileResult = getInstance().files()
					.create(filedata, new InputStreamContent(file.getContentType(), new ByteArrayInputStream(file.getBytes())))
					.setFields("id")
					.execute();
			
			return fileResult.getId();
	    }
	    
	    return null;
	}
	
	private String findOrCreateFolder(String parentId, String folderName) throws Exception {
		  String folderId = searchForFiles(parentId, folderName);
		  // Folder already exists, so return id
		  if (folderId != null) {
		     return folderId;
		  }
		  //Folder dont exists, create it and return folderId
		  File fileMetadata = new File();
		  fileMetadata.setMimeType("application/vnd.google-apps.folder");
		  fileMetadata.setName(folderName);
		 
		  if (parentId != null) {
		     fileMetadata.setParents(Collections.singletonList(parentId));
		  }
		  return getInstance().files().create(fileMetadata)
		        .setFields("id")
		        .execute()
		        .getId();
		}
	
	private String getFolderId(String path) throws Exception {
		  String parentId = null;
		  String[] folderNames = path.split("/");
		  
		  for (String name : folderNames) {
		     parentId = findOrCreateFolder(parentId, name);
		  }
		  return parentId;
		}
	
	public void downloadFile(String fileId) throws Exception{
		File fileList = getInstance().files().get(fileId).execute();
		String fileName = fileList.getName();
		System.out.println("file name " + fileName);

		java.io.File file = new java.io.File("C:/Users/User/downloadedfile/" + fileName + ".jpg");
		
		OutputStream fileStream = new FileOutputStream(file);
		
		getInstance().files().get(fileId).executeMediaAndDownloadTo(fileStream);
		
		fileStream.flush();
		fileStream.close();
		
	}
	
	public void deleteImage(String fileId) throws Exception {
		
		getInstance().files().delete(fileId).execute();
	}
	
	public File readImage(String fileId) throws Exception {
		File file = getInstance().files().get(fileId).execute();
		
		return file;
	}
}
