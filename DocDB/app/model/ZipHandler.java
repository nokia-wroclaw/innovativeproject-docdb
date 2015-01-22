package model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class ZipHandler {

	private void unzip(String source, String destination) {

		ZipFile zipFile;
		try {
			zipFile = new ZipFile(source);
			if (zipFile.isEncrypted()) {
				throw (new ZipException("Zip is encrypted"));
			}
			zipFile.extractAll(destination);

		} catch (ZipException e) {
			e.printStackTrace();
		}

	}

	ArrayList<String> handleZip(String source, String destination) {
		ZipHandler zip = new ZipHandler();
		zip.unzip(source, destination);
		ArrayList<String> filesPaths = new ArrayList<String>();
		File dir = new File(destination);
		try {
			List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			// going thru all dir
			for (File file : files) {
				filesPaths.add(file.getCanonicalPath());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return filesPaths;
	}
}