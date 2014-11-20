package model;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.google.common.io.Files;
import org.elasticsearch.client.Client;
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
	public void handleFile(FilePart uploadedFile) {

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
		String[] parsedFile = fileParser.parseFile(newFile, newPath);
		if (parsedFile != null) {
			Map json = esm.putJsonDocument(parsedFile);
			esm.insert(elasticServer.client, json, "twitter", "tweet");
			Logger.info("metadata saved");
		}

	}

}