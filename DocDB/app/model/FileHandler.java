package model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
	public void handleFile(FilePart uploadedFile, ArrayList<String> tagList) {
		File file = uploadedFile.getFile();
		String uploadedFileName = uploadedFile.getFilename();

		handleFile(file, uploadedFileName, tagList);

	}

	public void handleFile(File uploadedLink, ArrayList<String> tagList) {
		handleFile(uploadedLink, uploadedLink.getName(), tagList);
	}

	/**
	 * @param tagList
	 * @param file
	 * @param uploadedFileName
	 */
	private void handleFile(File file, String uploadedFileName, ArrayList<String> tagList) {
		// get new file hash
		String newFileCheckSum = getHash(file);

		String newFileName = uploadedFileName;
		File destFile = new File(newFileName);

		if (fileExists(newFileCheckSum)) {
			// file exists with the same name:
			if (destFile.exists()) {
				Logger.info("same file already exists: " + newFileName);
				return;
			}
			// file exist with different name:
			newFileName = dirPath + getExistingFileName(newFileCheckSum);

			ArrayList<String> parsedFile = fileParser.parseFile(new File(newFileName), newFileName, uploadedFileName);
			if (parsedFile != null) {
				insertToElastic(tagList, newFileCheckSum, parsedFile);
				Logger.info("metadata saved");
				return;
			}

		}

		// check if filename already exists
		int number = 0;
		while (destFile.exists()) {// file exists. need new name
			number++;
			destFile = new File(dirPath + number + uploadedFileName);
		}
		if(number == 0)
			destFile = new File(dirPath + uploadedFileName);
		newFileName = dirPath + uploadedFileName;

		try {
			Files.move(file, destFile);
			Logger.info("file saved");
		} catch (IOException e) {
			Logger.info("file save failed");
			e.printStackTrace();
		}

		ArrayList<String> parsedFile = fileParser.parseFile(destFile, newFileName, uploadedFileName);
		if (parsedFile != null) {
			insertToElastic(tagList, newFileCheckSum, parsedFile);
			Logger.info("metadata saved");
		}
	}

	/**
	 * @param tagList
	 * @param newFileCheckSum
	 * @param parsedFile
	 */
	private void insertToElastic(ArrayList<String> tagList, String newFileCheckSum, ArrayList<String> parsedFile) {
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
		ClusterHealthResponse healthResponse = elasticServer.client.admin().cluster().prepareHealth()
				.setWaitForGreenStatus().execute().actionGet();
		ClusterHealthStatus healthStatus = healthResponse.getStatus();
		/*if (!healthStatus.equals("GREEN")) {
			Logger.info("Waiting for GREEN or YELLOW status, now it is: " + healthStatus);
			//elasticServer.client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
		}*/
		Logger.info("Elastic is " + healthStatus);
		if (elasticServer.client.admin().indices().prepareExists("documents").execute().actionGet().isExists() == false)
			return null;
		ArrayList<ArrayList<String>> searchResult = elasticServer.elasticSearch.search(elasticServer.client, MD5,
				"documents", "file", fields, true);
		if (searchResult == null)
			return null;
		return searchResult.get(0).get(1);
	}

}