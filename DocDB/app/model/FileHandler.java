package model;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.google.common.io.Files;
import org.elasticsearch.client.Client;
import play.Logger;
import play.mvc.Http.MultipartFormData.FilePart;

public class FileHandler {

	private FileParser fileParser;
	private ElasticSearchManager esm;
    private static String dirPath = "files/";
	
	public FileHandler() {
		fileParser = new FileParser();
		esm = new ElasticSearchManager();
	}
	
	public void handleFile(FilePart uploadedFile, Client client ) {

//		String fileName = uploadedFile.getFilename();
//		String contentType = uploadedFile.getContentType(); 

		File file = uploadedFile.getFile();
		String newPath=dirPath + uploadedFile.getFilename();
		File newFile = new File(newPath);
		
		try {
			Files.move(file, newFile);
			Logger.info("file saved in: "+newPath);
		} catch (IOException e) {
			Logger.info("file save failed");
			e.printStackTrace();
		}
		
		String[] parsedFile = fileParser.parseFile(newFile);
		if (parsedFile!=null){
			Map json = esm.putJsonDocument(parsedFile);
			esm.insert(client, json, "twitter", "tweet");
			Logger.info("metadata saved");
		}

	}


}
