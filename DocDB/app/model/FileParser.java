package model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.tika.*;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Purpose of this class is taking file from File handler, parsing it with Tika
 * Apache, then giving back the result to handler
 * 
 * @author a. dyngosz. s.majkrzak, m.wierzbicki
 * 
 */
public class FileParser {

	/**
	 * With using Tika Apache, this method takes all data from file, handle it
	 * in Metadata class object and BodyContentHandler class object, gives it to
	 * dataToArray method, and return final result to File Handler
	 * 
	 * @param fileToParse
	 *            file given by client, which have to be parsed
	 */
	public ArrayList <String> parseFile(File fileToParse, String newPath) {
		InputStream is = null;

		try {
			String fileName = fileToParse.getName();
			is = new BufferedInputStream(new FileInputStream(fileToParse));
			Parser parser = new AutoDetectParser();
			ContentHandler handler = new BodyContentHandler();	//it keeps content of document
			Metadata metadata = new Metadata();		//all metadata stored in file
			
			parser.parse(is, handler, metadata); // do versii 0.3
			// parser.parse(is, handler, metadata, new ParseContext());
			String size = fileToParse.length() / 1024 + "";
			ArrayList <String> result = dataToArray(metadata, handler, fileName, size);
			Path path = Paths.get(newPath); 
			result.add("#"+Files.probeContentType(path));
			
			return result;

		} catch (IOException e) {
			e.printStackTrace();
		} catch (TikaException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;

	}

	/**
	 * Extract the most important data for Elastic search and creates array with
	 * them
	 * 
	 * @param metadata
	 *            metadata of given file
	 * @param handler
	 *            object with content of file
	 * @param fileName
	 *            name of file used to creating temporary path of file
	 * @param size
	 * 			  size of given file in kb
	 * @return Array with all interesting data for us
	 */
	public ArrayList <String> dataToArray(Metadata metadata, ContentHandler handler,
			String fileName, String size) {
		ArrayList <String> data = new ArrayList <String> ();
		//String[] data = new String[5];
		if (metadata.get("title") != null && metadata.get("title").length() != 0)
			data.add(metadata.get("title"));
		else
			data.add(fileName);

		if (metadata.get("Author") != null&& metadata.get("Author").length() != 0)
			data.add(metadata.get("Author"));
		else
			data.add("No_author");
		data.add(handler.toString()); // content of file
		data.add(fileName);
		data.add(size);
		/*for (int i = 0; i < 5; i++) {
			if (i == 2) {
				continue;
			}
			System.out.println(data[i]);
		}*/
		return data;
	}
}