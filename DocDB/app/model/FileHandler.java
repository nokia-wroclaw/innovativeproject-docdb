package model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
	public void handleFile(FilePart uploadedFile, Set<String> tagList, String[] locationCoordinates) {
		File file = uploadedFile.getFile();
		String uploadedFileName = uploadedFile.getFilename();
		handleFile(file, uploadedFileName, tagList, locationCoordinates);
	}

	public void handleFile(File uploadedLink, Set<String> tagList) {
		handleFile(uploadedLink, uploadedLink.getName(), tagList, null);
	}

	/**
	 * @param file
	 * @param uploadedFileName
	 * @param tagSet
	 * @param locationCoordinates
	 *            TODO
	 */
	private void handleFile(File file, String uploadedFileName, Set<String> tagSet, String[] locationCoordinates) {
		// get new file hash
		String newFileCheckSum = getHash(file);
		String newFileName = dirPath + uploadedFileName;
		File destFile = new File(newFileName);
		String[] imageLocationCoordinates = null;
		 
		GeolocationExtractor gextractor = new GeolocationExtractor();
		if (fileExists(newFileCheckSum)) {
			// file exists with the same name:
			if (destFile.exists()) {
				Logger.info("Same file already exists: " + newFileName);
				return;
			}
			// file exist with different name:
			newFileName = dirPath + getExistingFileName(newFileCheckSum);
			ArrayList<String> parsedFileData = fileParser.parseFile(new File(newFileName), uploadedFileName, "");
			if (uploadedFileName.endsWith(".zip")) {
				handleZip(tagSet, newFileCheckSum, uploadedFileName, locationCoordinates);
				Logger.info("Metadata saved");
				return;
			}
			if (uploadedFileName.endsWith(".jpg")) {
				handleJPG(tagSet, file);
				imageLocationCoordinates = gextractor.latitudeExtractor(destFile);
				
			}
			
			if (parsedFileData != null) {
				tagSet.addAll(getFileType(uploadedFileName));
				if (imageLocationCoordinates != null)
					locationCoordinates = imageLocationCoordinates;
				insertToElastic(tagSet, newFileCheckSum, parsedFileData, locationCoordinates);
				Logger.info("Metadata saved");
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
			handleZip(tagSet, newFileCheckSum, uploadedFileName, locationCoordinates);
			Logger.info("metadata saved");
			return;
		}
		ArrayList<String> parsedFile = fileParser.parseFile(destFile, uploadedFileName, "");

		if (uploadedFileName.endsWith(".jpg")) {
			handleJPG(tagSet, destFile);
			imageLocationCoordinates = gextractor.latitudeExtractor(destFile);
			System.out.println(imageLocationCoordinates);
		}
		if (parsedFile != null) {
			tagSet.addAll(getFileType(uploadedFileName));
			if (imageLocationCoordinates != null)
				locationCoordinates = imageLocationCoordinates;
			insertToElastic(tagSet, newFileCheckSum, parsedFile, locationCoordinates);
			Logger.info("metadata saved");
		}
	}

	private void handleZip(Set<String> tagList, String newFileCheckSum, String newFileName, String[] locationCoordinates) {
		ZipHandler zipHandler = new ZipHandler();
		ArrayList<String> zipFilesNames = zipHandler.handleZip(newFileName, dirPath + "zip/");
		for (String curFileName : zipFilesNames) {
			Set<String> tagTemp = getFileType(curFileName);
			tagTemp.add("zip");
			tagTemp.addAll(tagList);
			ArrayList<String> parsedFile = fileParser.parseFile(new File(curFileName), curFileName, newFileName);
			insertToElastic(tagTemp, newFileCheckSum, parsedFile, locationCoordinates);
		}
		zipHandler.removeUnpackedZip(dirPath + "zip/");

	}

	private void handleJPG(Set<String> tagList, File destFile) {
		GeolocationExtractor gextractor = new GeolocationExtractor();
		String photoGeolocation = "";
		try {
			photoGeolocation = gextractor.locationExtractor(destFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!photoGeolocation.equals(""))
			tagList.add(photoGeolocation.split(", "));

	}

	/**
	 * @param tagList
	 * @param newFileCheckSum
	 * @param parsedFile
	 * @param locationCoordinates
	 *            TODO
	 */
	private void insertToElastic(Set<String> tagList, String newFileCheckSum, ArrayList<String> parsedFile,
			String[] locationCoordinates) {
		
		parsedFile.add(newFileCheckSum);
		XContentBuilder json = elasticServer.elasticSearch.putJsonDocument(parsedFile, tagList, locationCoordinates);
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

	private Set getFileType(String oldPath) {
		Set<String> result = new HashSet<String>();
		Path path = Paths.get(oldPath);
		String fileType = Files.getFileExtension(oldPath);
		String[] temp = fileType.split("/");
		for (int s = 0; s < temp.length; s++) {
			result.add(temp[s]);
		}
		return result;
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
