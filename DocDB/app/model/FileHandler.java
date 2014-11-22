package model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.google.common.io.Files;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;

import play.Logger;
import play.mvc.Http.MultipartFormData.FilePart;
/**
 * Class control flow of file during uploading, parsing and sending it to elastic search database.
 * @author a.dyngosz, s.majkrzak. m. wierzbicki
 */
public class FileHandler {

	private FileParser fileParser;
	private ElasticSearchManager esm;
	private ElasticSearchServer elasticServer;
	private static String dirPath = "files/";

	public FileHandler(ElasticSearchServer elasticServer) {
		this.elasticServer = elasticServer;
		fileParser = new FileParser();
		esm = new ElasticSearchManager();
	}

	/**
	 * ClientWebSocket invoke this method, when user wants to send files to server. It takes care of giving it to Tika Apache
	 * parser, getting back array with content and metadata. Next, method receive the map (requiered for Elastic search) and 
	 * send it to ES server
	 * 
	 * @param uploadedFile file given by user to upload
	 */
	public void handleFile(FilePart uploadedFile, ArrayList <String> tagsArray) {

		// String fileName = uploadedFile.getFilename();
		// String contentType = uploadedFile.getContentType();

		File file = uploadedFile.getFile();
		String newPath = dirPath + uploadedFile.getFilename();
		File newFile = new File(newPath);

		try {
			Files.move(file, newFile);
			Logger.info("file saved in: " + newPath);
		} catch (IOException e) {
			Logger.info("file save failed");
			e.printStackTrace();
		}
		ArrayList <String> parsedFile = fileParser.parseFile(newFile, newPath);
		if (parsedFile != null) {
			//musze usunac tagi dotyczace plikow z parsedFile i
			//przeniesc je do tagsArray
			String temp = parsedFile.get(parsedFile.size() - 1);
			parsedFile.remove(parsedFile.size() - 1);
			tagsArray.add(temp);
			XContentBuilder json = esm.putJsonDocument(parsedFile, tagsArray);
			esm.insert(elasticServer.client, json, "twitter", "tweet");
			Logger.info("metadata saved");
		}

	}

}