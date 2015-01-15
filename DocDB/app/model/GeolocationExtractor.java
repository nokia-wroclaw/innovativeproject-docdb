package model;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.text.Normalizer;

import org.json.JSONException;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This class is used to read exifs from *.jpg and *.jpeg. If there are
 * geolocation tags, it will convert it to name of place, using Google API
 * 
 * @author a.dyngosz, s.majkrzak, m.wierzbicki
 */
public class GeolocationExtractor {

	/**
	 * Read geolocation tags from file and convert it to adress
	 * 
	 * @param file
	 *            photo, in which you want to search geolocation tags
	 * @return location_string name of place, in which photo was taken
	 * @throws IOException
	 *             no file found
	 * @throws ImageProcessingException
	 */
	public String extractor(File file) throws IOException {
		String location_string = "";
		String[] latlng = {};
		Double lat, lng;
		JsonElement ret;
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(file);
			// See whether it has GPS data
			GpsDirectory gpsDirectory = metadata.getDirectory(GpsDirectory.class);
			// Try to read out the location, making sure it's non-zero
			if (gpsDirectory != null) {
				GeoLocation geoLocation = gpsDirectory.getGeoLocation();
				System.out.println(geoLocation.toString());
				if (!geoLocation.isZero()) {
					latlng = geoLocation.toString().split(", ");

				}
			} else {
				return "";
			}

			lat = Double.valueOf(latlng[0]);
			lng = Double.valueOf(latlng[1]);
			ret = getLocationInfo(lat, lng);

			location_string = getPlaceName(ret);
			return location_string;
		} catch (NullPointerException e2) {
			return location_string;
		} catch (ImageProcessingException e) {
			return location_string;
		}
		// return location_string;
	}

	/**
	 * Using Google API this method read information about a place, and return
	 * it.
	 * 
	 * @param lat
	 *            latitude of a place
	 * @param lng
	 *            longtitude of a place
	 * @return JSON with all data about this place
	 */
	public JsonElement getLocationInfo(double lat, double lng) {

		System.out.println("lat: " + lat + " lang: " + lng);
		String url = "http://maps.google.com/maps/api/geocode/json?latlng=" + lat + "," + lng;
		InputStream openStream;
		try {
			openStream = new URL(url).openStream();
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
		JsonElement json = readJson(openStream);
		return json;
	}

	private JsonElement readJson(InputStream openStream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(openStream, Charsets.UTF_8));
		JsonElement json = new JsonParser().parse(reader);
		return json;
	}

	public String getPlaceName(JsonElement location) {
		String location_string = null;
		if (location.isJsonNull() == false) {
			JsonObject jsonobject = location.getAsJsonObject();
			JsonArray resultArray = jsonobject.get("results").getAsJsonArray();
			JsonObject addressComponents = resultArray.get(0).getAsJsonObject();
			JsonElement addressComponentsElement = addressComponents.get("formatted_address");
			location_string = addressComponentsElement.toString();
			location_string = Normalizer.normalize(location_string, Normalizer.Form.NFD)
					.replaceAll("[^\\p{ASCII}]", ""); //removing polish signs 
			location_string = Normalizer.normalize(location_string, Normalizer.Form.NFD)
					.replaceAll("[\"]", "");//removing " sign

		}
		return location_string;
	}
}
