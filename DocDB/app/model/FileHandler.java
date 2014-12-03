package model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.elasticsearch.common.xcontent.XContentBuilder;

import play.Logger;
import play.mvc.Http.MultipartFormData.FilePart;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

/**
 * Class control flow of file during uploading, parsing and sending it to
 * elastic search database.
 * 
 * @author a.dyngosz, s.majkrzak. m. wierzbicki
 */
public class FileHandler {

	private final FileParser fileParser;
	private final ElasticSearchServer elasticServer;
	// private final MD5Checksum md5;
	private static String dirPath = "files/";

	public FileHandler(ElasticSearchServer elasticServer) {
		this.elasticServer = elasticServer;
		fileParser = new FileParser();
		// md5 = new MD5Checksum();
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
		// String fileName = uploadedFile.getFilename();
		// String contentType = uploadedFile.getContentType();

		File file = uploadedFile.getFile();
		String newPath = dirPath + uploadedFile.getFilename();
		String oldPath = dirPath + uploadedFile.getFilename();
		File newFile = new File(newPath);

		// get new file hash
		String newFileCheckSum = null;
		try {
			newFileCheckSum = Files.hash(file, Hashing.md5()).toString();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (fileExists(newFileCheckSum)) {
			// file exists with the same name:
			if (new File(newPath).exists()) {
				Logger.info("same file already exists: " + newPath);
				return;
			}
			// file exist with different name:
			newPath = dirPath + getExistingFileName(newFileCheckSum);
			ArrayList<String> parsedFile = fileParser.parseFile(new File(newPath), newPath, oldPath);
			if (parsedFile != null) {
				// musze usunac tagi dotyczace plikow z parsedFile i
				// przeniesc je do tagsArray
				extractTags(tagsArray, newFileCheckSum, parsedFile);
				XContentBuilder json = elasticServer.elasticSearch.putJsonDocument(parsedFile, tagsArray);
				elasticServer.elasticSearch.insert(elasticServer.client, json, "documents", "file");
				Logger.info("metadata saved");
				return;
			}

		}

		// check if filename already exists
		int number = 0;
		while (newFile.exists()) {// file exists. need new name
			number++;
			newFile = new File(number + newPath);
		}
		newPath = dirPath + number + uploadedFile.getFilename();

		try {
			Files.move(file, newFile);
			Logger.info("file saved");
		} catch (IOException e) {
			Logger.info("file save failed");
			e.printStackTrace();
		}

		ArrayList<String> parsedFile = fileParser.parseFile(newFile, newPath, oldPath);
		if (parsedFile != null) {
			// musze usunac tagi dotyczace plikow z parsedFile i
			// przeniesc je do tagsArray
			extractTags(tagsArray, newFileCheckSum, parsedFile);
			XContentBuilder json = elasticServer.elasticSearch.putJsonDocument(parsedFile, tagsArray);
			elasticServer.elasticSearch.insert(elasticServer.client, json, "documents", "file");
			Logger.info("metadata saved");
		}

	}

	/**
	 * @param tagsArray
	 * @param newFileCheckSum
	 * @param parsedFile
	 */
	private void extractTags(ArrayList<String> tagsArray, String newFileCheckSum, ArrayList<String> parsedFile) {
		String temp = parsedFile.get(parsedFile.size() - 1);
		parsedFile.remove(parsedFile.size() - 1);
		tagsArray.add(temp);
		parsedFile.add(newFileCheckSum);
	}

	private boolean fileExists(String MD5) {
		return getExistingFileName(MD5) != null;
	}

	private String getExistingFileName(String MD5) {
		String[] fields = { "MD5" };
		if (elasticServer.client.admin().indices().prepareExists("documents").execute().actionGet().isExists() == false)
			return null;
		ArrayList<ArrayList<String>> searchResult = elasticServer.elasticSearch.search(elasticServer.client, MD5,
				"documents", "file", fields);
		if (searchResult == null)
			return null;
		return searchResult.get(0).get(1);
	}

}