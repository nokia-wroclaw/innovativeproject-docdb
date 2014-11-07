package model;

import java.io.File;

public class FileHandler {

	private FileParser fileParser;
	private ElasticSearchManager esm;
	
	public FileHandler() {
		fileParser = new FileParser();
		esm = new ElasticSearchManager();
	}
	
	public void handleFile(File file) {
		String[] parsedFile = fileParser.parseFile(file);
		if (parsedFile!=null){
			esm.putJsonDocument(parsedFile);
		}

	}

}
