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

public class GeolocationExtractor {
	
	public String extractor (File file) throws IOException, ImageProcessingException {
		 Metadata metadata = ImageMetadataReader.readMetadata(file);
	       // See whether it has GPS data
	        GpsDirectory gpsDirectory = metadata.getDirectory(GpsDirectory.class);
	        // Try to read out the location, making sure it's non-zero
	        GeoLocation geoLocation = gpsDirectory.getGeoLocation();
	        
	        String [] latlng = geoLocation.toString().split(", ");
	        Double lat = Double.valueOf(latlng [0]);
	        Double lng = Double.valueOf(latlng [1]);
	        JSONObject ret = getLocationInfo(lat, lng); 
	        JSONObject location;
	        String location_string = "";
	        try {
	            //Get JSON Array called "results" and then get the 0th complete object as JSON        
	            location = ret.getJSONArray("results").getJSONObject(0); 
	            // Get the value of the attribute whose name is "formatted_string"
	            location_string = location.getString("formatted_address");
	            return (location_string);
	        } catch (JSONException e1) {
	            e1.printStackTrace();

	        }
	        return location_string;
	}
	
	public static JSONObject getLocationInfo( double lat, double lng) {

        HttpGet httpGet = new HttpGet("http://maps.google.com/maps/api/geocode/json?latlng="+lat+","+lng+"&sensor=false");
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
