package model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import play.Logger;
import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.WebSocket;
import akka.actor.UntypedActor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ClientWebSocket extends UntypedActor {

	private final WebSocket.In<JsonNode> socketIn;
	private final WebSocket.Out<JsonNode> socketOut;
	private final ElasticSearchServer elasticServer;
	private final ContextExtractor ctxEx;

	ClientWebSocket(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out, ElasticSearchServer elasticServer) {

		Logger.info("New ClientWebSocket");
		this.socketIn = in;
		this.socketOut = out;
		this.elasticServer = elasticServer;
		ctxEx = ContextExtractor.getInstance();
		socketIn.onMessage(new Callback<JsonNode>() { // msg z socketu
			@Override
			public void invoke(JsonNode event) {
				handleEvent(event);
			}
		});

		socketIn.onClose(new Callback0() { // socket sie zamkna
			@Override
			public void invoke() {
				// stop actor
				getContext().stop(getSelf());
			}
		});
	}

	protected void handleEvent(JsonNode event) { // obslug eventow z websocketa

		if (!event.has("request")) {
			return;
		}
		String request = event.get("request").asText();

		if (request.equals("geolocation")) {
			handleGeolocation(event);
			return;
		} else if (request.equals("search")) {

			// get request data
			String pattern = event.get("pattern").asText();
			Boolean limit = event.get("limit").asText().equals("true");
			List<String> tagList = ctxEx.extractTags(pattern);
			String searchPattern = ctxEx.stripTags(pattern);

			Logger.info("searching for:" + pattern + "with" + (limit ? "out" : "") + " limit");
			Logger.info("search:" + searchPattern + "\ntags:" + tagList.toString());

			// search elasticSearch search
			ArrayList<ArrayList<String>> searchResult = search(searchPattern, limit);

			if (!tagList.isEmpty()) searchResult = filterOutByTags(searchResult, tagList);

			if (searchResult == null) {
				sendEmptyResults();
			} else {
				Logger.info(String.valueOf(searchResult.size()) + " found");

				ObjectNode message = Json.newObject(); // create message
				ArrayNode results = message.putArray("result"); // results array in message

				for (ArrayList<String> result : searchResult) {
					ObjectNode innerMsg = Json.newObject(); // inner message (file info)
					innerMsg.put("file", result.get(0));
					innerMsg.put("link", "Download/" + result.get(1));
					innerMsg.put("size", result.get(2));
					innerMsg.put("context", ctxEx.getContext(result.get(3), pattern));

					ArrayNode tags = innerMsg.putArray("tags"); // tags array in innerMsg (for this file)

					int tagcount = result.size() - 4;
					for (int tagnr = 0; tagnr < tagcount; tagnr++) {
						tags.add(result.get(4 + tagnr));
					}
					// innerMsg.put("tags", tags);

					results.add(innerMsg);
				}
				socketOut.write(message);
			}
		}
	}

	/**
	 * 
	 */
	private void sendEmptyResults() {
		ObjectNode message = Json.newObject();
		message.put("result", "{}");
		Logger.info("No results");
		socketOut.write(message);
	}

	/**
	 * @param event
	 */
	private void handleGeolocation(JsonNode event) {
		GeolocationExtractor geoExtractor = new GeolocationExtractor();
		double lat = Double.parseDouble(event.get("lat").asText());
		double lng = Double.parseDouble(event.get("lng").asText());
		String location = "";
		System.out.println("lat" + lat);
		System.out.println("lng" + lng);
		try {
			JSONObject jo = geoExtractor.getLocationInfo(lat, lng);
			location = geoExtractor.getPlaceName(jo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ObjectNode message = Json.newObject();
		message.put("geo", location);
		socketOut.write(message);
		Logger.info(location + "witam");
	}

	private ArrayList<ArrayList<String>> filterOutByTags(ArrayList<ArrayList<String>> searchResult, List<String> tags) {
		ArrayList<ArrayList<String>> resultList = new ArrayList<>();

		for (ArrayList<String> result : searchResult) {
			int tagcount = result.size() - 4;
			for (int tagnr = 0; tagnr < tagcount; tagnr++) {
				for (String tag : tags) {
					if (result.get(tagnr + 4).equals(tag)) {
						resultList.add(result);
						tagnr = tagcount;// break the second loop
						break;
					}
				}
			}
		}
		return resultList;
	}

	private ArrayList<ArrayList<String>> search(String pattern, Boolean limit) {
		ArrayList<ArrayList<String>> searchResult = elasticServer.elasticSearch.search(elasticServer.client, pattern,
				"documents", "file", limit);
		return searchResult;
	}

	@Override
	public void onReceive(Object arg0) throws Exception { // msg od innych
															// aktorów /
															// systemu

	}
}
