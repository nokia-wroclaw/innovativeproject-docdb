package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.elasticsearch.client.Client;

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
			public void invoke(JsonNode event) {
				handleEvent(event);
			}
		});

		socketIn.onClose(new Callback0() { // socket sie zamkna
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
			// TODO: get tags from H2
			List<String> tagList = null;
			String searchPattern = null;

			Logger.info("searching for:" + pattern + " with" + (limit ? "out" : "") + " limit");
			Logger.info("search:" + searchPattern + "\ntags:" + tagList.toString());

			ArrayList<ArrayList<String>> searchResult = search(searchPattern, limit);
			//TODO: 
				// Filter by tags
				// add remaining data to result from H2(geoLoc, tags, author, date, name etc)
			if (searchResult == null) {
				sendEmptyResults();
			} else {
				Logger.info(String.valueOf(searchResult.size()) + " found");

				ObjectNode message = Json.newObject(); // create message
				ArrayNode results = message.putArray("result"); // results array in message
				Set<String> tagsSet = new HashSet<String>();

				for (ArrayList<String> result : searchResult) {
					ObjectNode innerMsg = Json.newObject(); // inner message (file info)
					innerMsg.put("content", result.get(0));
					innerMsg.put("id", result.get(1));		
					results.add(innerMsg);
				}
				// 'content' is already highlighted text by searched phrase
				message.put("resultsCount", "" + searchResult.size());
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
		String lat = event.get("lat").asText();
		String lng = event.get("lng").asText();
		String location = null;
		try {
			location = geoExtractor.getPlaceName(geoExtractor.getLocationInfo(lat, lng));
		} catch (Exception e) {
			e.printStackTrace();
		}
		ObjectNode message = Json.newObject();
		message.put("geo", location);
		socketOut.write(message);
	}

	private ArrayList<ArrayList<String>> search(String phrase, Boolean limit) {
		ArrayList<ArrayList<String>> searchResult = elasticServer.elasticSearch.search(elasticServer.client, phrase,
				"documents", "file", "content", limit);
		return searchResult;
	}

	@Override
	public void onReceive(Object arg0) throws Exception {
	}
}
