package model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import com.google.common.io.Files;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;

import play.Logger;
import play.mvc.Http.MultipartFormData.FilePart;

/**
 * Class control flow of file during uploading, parsing and sending it to
 * elastic search database.
 * 
 * @author a.dyngosz, s.majkrzak. m. wierzbicki
 */
public class FileHandler {

	private FileParser fileParser;
	private ElasticSearchManager esm;
	private ElasticSearchServer elasticServer;
	private MD5Checksum md5;
	private static String dirPath = "files/";

	public FileHandler(ElasticSearchServer elasticServer) {
		this.elasticServer = elasticServer;
		fileParser = new FileParser();
		esm = new ElasticSearchManager();
		md5 = new MD5Checksum();
	}

	/**
	 * Controller invokes this method, when user wants to send files to server.
	 * It takes care of giving it to Tika Apache parser, getting back array with
	 * content and metadata. Next, method receive the map (requiered for Elastic
	 * search) and send it to ES server
	 * 
	 * @param uploadedFile
	 *            file given by user to upload
	 */
	public void handleFile(FilePart uploadedFile, ArrayList<String> tagsArray) {
		int number = 0;
		// String fileName = uploadedFile.getFilename();
		// String contentType = uploadedFile.getContentType();

		File file = uploadedFile.getFile();
		String newPath = dirPath + uploadedFile.getFilename();
		File newFile = new File(newPath);

		// get new file md5
		String newFileCheckSum = null;
		try {
			newFileCheckSum = md5.getMD5Checksum(file);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// check if filename already exists
//		while (newFile.exists()) {// file exists. need new name
//			try {
//				if (md5.getMD5Checksum(newFile).equals(newFileCheckSum)) {
//					// unless content is the same
//					Logger.info("same file already exists: " + newPath);
//					return; // no need to have 2 same files
//				}
//			} catch (Exception e2) {
//				e2.printStackTrace();
//			}
//			number++;
//			newFile = new File(newPath + number);
//		}
		String oldPath = newPath;
		newPath = newPath + number;
		String[] fieldNames = {"MD5"};
		searchMan.search(
				elasticServer.client, pattern, "twitter", "tweet");
		try {
			Files.move(file, newFile);
			Logger.info("file saved");
		} catch (IOException e) {
			Logger.info("file save failed");
			e.printStackTrace();
		}
		ArrayList<String> parsedFile = fileParser.parseFile(newFile, newPath,
				oldPath);
		if (parsedFile != null) {
			// musze usunac tagi dotyczace plikow z parsedFile i
			// przeniesc je do tagsArray
			String temp = parsedFile.get(parsedFile.size() - 1);
			parsedFile.remove(parsedFile.size() - 1);
			tagsArray.add(temp);
			parsedFile.add(newFileCheckSum);
			XContentBuilder json = esm.putJsonDocument(parsedFile, tagsArray);
			esm.insert(elasticServer.client, json, "twitter", "tweet");
			Logger.info("metadata saved");
		}

	}
	
	private ArrayList<ArrayList<String>> search(String pattern, ) {
		ArrayList<ArrayList<String>> searchResult = elasticServer.elasticSearch.search(
				elasticServer.client, pattern, "twitter", "tweet");
		return searchResult;
	}

}