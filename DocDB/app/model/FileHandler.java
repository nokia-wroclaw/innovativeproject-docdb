package model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
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
	public void handleFile(FilePart uploadedFile, Set<String> tagList) {
		File file = uploadedFile.getFile();
		String uploadedFileName = uploadedFile.getFilename();
		handleFile(file, uploadedFileName, tagList);
	}

	public void handleFile(File uploadedLink, Set<String> tagList) {
		handleFile(uploadedLink, uploadedLink.getName(), tagList);
	}

	/**
	 * @param tagList
	 * @param file
	 * @param uploadedFileName
	 */
	private void handleFile(File file, String uploadedFileName, Set<String> tagList) {
		// get new file hash

		String newFileCheckSum = getHash(file);
		String newFileName = dirPath + uploadedFileName;
		File destFile = new File(newFileName);
		if (fileExists(newFileCheckSum)) {
			// file exists with the same name:
			if (destFile.exists()) {
				Logger.info("same file already exists: " + newFileName);
				return;
			}
			// file exist with different name:
			newFileName = dirPath + getExistingFileName(newFileCheckSum);
			ArrayList<String> parsedFile = fileParser.parseFile(new File(newFileName), uploadedFileName, "");

			if (uploadedFileName.endsWith(".zip")) {
				handleZip(tagList, newFileCheckSum, uploadedFileName);
				Logger.info("metadata saved");
				return;
			}
			if (uploadedFileName.endsWith(".jpg")) {
				handleJPG(tagList, file);
			}

			if (parsedFile != null) {
				insertToElastic(tagList, newFileCheckSum, parsedFile);
				Logger.info("metadata saved");
				return;
			}

		}

		// check if filename already exists
		int number = 0;
		while (destFile.exists()) {// file exists need new name
			number++;
			destFile = new File(dirPath + number + uploadedFileName);
		}
		if (number == 0)
			destFile = new File(dirPath + uploadedFileName);
		

		try {
			Files.move(file, destFile);
			Logger.info("file saved");
		} catch (IOException e) {
			Logger.info("file save failed");
			e.printStackTrace();
		}

		if (uploadedFileName.endsWith(".zip")) {
			handleZip(tagList, newFileCheckSum, uploadedFileName);
			Logger.info("metadata saved");
			return;
		}
		ArrayList<String> parsedFile = fileParser.parseFile(destFile, uploadedFileName, "");

		if (uploadedFileName.endsWith(".jpg")) {
			handleJPG(tagList, destFile);
		}
		if (parsedFile != null) {
			insertToElastic(tagList, newFileCheckSum, parsedFile);
			Logger.info("metadata saved");
		}
	}

	private void handleZip(Set<String> tagList, String newFileCheckSum, String newFileName) {
		ZipHandler zipHandler = new ZipHandler();
		ArrayList<String> zipFilesNames = zipHandler.handleZip(newFileName, dirPath +"zip/");
		for (String curFileName : zipFilesNames) {
			ArrayList<String> parsedFile = fileParser.parseFile(new File(curFileName), curFileName, newFileName);
			insertToElastic(tagList, newFileCheckSum, parsedFile);
		}

	}

	private void handleJPG(Set<String> tagList, File destFile) {
		GeolocationExtractor gextractor = new GeolocationExtractor();
		String photoGeolocation = "";
		try {
			photoGeolocation = gextractor.extractor(destFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		tagList.add(photoGeolocation);
	}

	/**
	 * @param tagList
	 * @param newFileCheckSum
	 * @param parsedFile
	 */
	private void insertToElastic(Set<String> tagList, String newFileCheckSum, ArrayList<String> parsedFile) {
		extractTags(tagList, newFileCheckSum, parsedFile);
		XContentBuilder json = elasticServer.elasticSearch.putJsonDocument(parsedFile, tagList);
		elasticServer.elasticSearch.insert(elasticServer.client, json, "documents", "file");
	}

	/**
	 * @param file
	 * @param newFileCheckSum
	 * @return
	 */
	private String getHash(File file) {
		String newFileCheckSum;
		try {
			newFileCheckSum = Files.hash(file, Hashing.md5()).toString();
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
		return newFileCheckSum;
	}

	/**
	 * @param tagList
	 * @param newFileCheckSum
	 * @param parsedFile
	 */
	private void extractTags(Set<String> tagList, String newFileCheckSum, ArrayList<String> parsedFile) {
		String temp = parsedFile.get(parsedFile.size() - 1);
		parsedFile.remove(parsedFile.size() - 1);
		tagList.add(temp);
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
				"documents", "file", fields, true);
		if (searchResult == null)
			return null;
		return searchResult.get(0).get(1);
	}

}