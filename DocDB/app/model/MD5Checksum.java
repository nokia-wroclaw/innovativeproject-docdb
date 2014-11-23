package model;

import java.io.*;
import java.security.MessageDigest;

public class MD5Checksum {

	/*
	 * Method return an array of bytes, that contain MD5 checksum of given file.
	 */
	public byte[] createChecksum(String filename) throws Exception {
		InputStream fis = new FileInputStream(filename);
		byte[] buffer = new byte[1024];
		MessageDigest md = MessageDigest.getInstance("MD5");

		int bytesReader;
		do {
			bytesReader = fis.read(buffer);
			if (bytesReader > 0)
				md.update(buffer, 0, bytesReader);
		} while (bytesReader != -1);

		fis.close();
		return md.digest();
	}
	public byte[] createChecksum(File file) throws Exception {
		InputStream fis = new FileInputStream(file);
		byte[] buffer = new byte[1024];
		MessageDigest md = MessageDigest.getInstance("MD5");

		int bytesReader;
		do {
			bytesReader = fis.read(buffer);
			if (bytesReader > 0)
				md.update(buffer, 0, bytesReader);
		} while (bytesReader != -1);

		fis.close();
		return md.digest();
	}

	/*
	 * Method use createChecksum() to generate byte array, and then makes a HEX
	 * string of it.
	 */
	public String getMD5Checksum(String filename) throws Exception {

		byte[] bytesArray = createChecksum(filename);
		String result = "";
		for (int i = 0; i < bytesArray.length; i++)
			result += Integer.toString((bytesArray[i] & 0xff) + 0x100, 16)
					.substring(1);

		return result;
	}
	public String getMD5Checksum(File file) throws Exception {

		byte[] bytesArray = createChecksum(file);
		String result = "";
		for (int i = 0; i < bytesArray.length; i++)
			result += Integer.toString((bytesArray[i] & 0xff) + 0x100, 16)
					.substring(1);

		return result;
	}
}