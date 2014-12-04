package model;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is used to read exifs from *.jpg and *.jpeg. If there are
 * geolocation tags, it will convert it to name of place, using Google API
 * 
 * @author a.dyngosz, s.majkrzak, m.wierzbicki
 */
public class GeolocationExtractor {
	Metadata metadata;
	GpsDirectory gpsDirectory;
	GeoLocation geoLocation;
	String[] latlng;
	Double lat, lng;
	JSONObject ret, location;
	String location_string = "";

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
	public String extractor(File file) throws IOException, ImageProcessingException {
		metadata = ImageMetadataReader.readMetadata(file);
		// See whether it has GPS data
		gpsDirectory = metadata.getDirectory(GpsDirectory.class);
		// Try to read out the location, making sure it's non-zero
		if (gpsDirectory != null) {
			geoLocation = gpsDirectory.getGeoLocation();
			latlng = geoLocation.toString().split(", ");
		} else {
			return "";
		}
		lat = Double.valueOf(latlng[0]);
		lng = Double.valueOf(latlng[1]);
		ret = getLocationInfo(lat, lng);
		try {
			// Get JSON Array called "results" and then get the 0th complete
			// object as JSON
			location = ret.getJSONArray("results").getJSONObject(0);
			// Get the value of the attribute whose name is "formatted_string"
			location_string = location.getString("formatted_address");
			return location_string;
		} catch (JSONException e1) {
			e1.printStackTrace();

		}
		return location_string;
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
	public static JSONObject getLocationInfo(double lat, double lng) {

		HttpGet httpGet = new HttpGet("http://maps.google.com/maps/api/geocode/json?latlng=" + lat + "," + lng + "&sensor=false");
		HttpClient client = new DefaultHttpClient();
		HttpResponse response;
		StringBuilder stringBuilder = new StringBuilder();

		try {
			response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			int b;
			while ((b = stream.read()) != -1) {
				stringBuilder.append((char) b);
			}
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		}

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = new JSONObject(stringBuilder.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
}
