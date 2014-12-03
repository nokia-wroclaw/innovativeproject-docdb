package model;

import java.util.ArrayList;
import java.util.List;

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

		if (!request.equals("search")) {
			return;
		}

		String pattern = event.get("pattern").asText();
		Logger.info("searching for:" + pattern);

		List<String> tagList = ctxEx.extractTags(pattern);
		String searchPattern = ctxEx.stripTags(pattern);

		Logger.info("search:" + searchPattern);
		Logger.info("tags:" + tagList.toString());

		ArrayList<ArrayList<String>> searchResult = search(searchPattern);
		if (searchResult == null) {
			if (!tagList.isEmpty())
				searchResult = filterOutByTags(searchResult, tagList);
			ObjectNode message = Json.newObject();
			message.put("result", "{}");
			Logger.info("No results");
			socketOut.write(message);
			return;
		}

		ObjectNode message = Json.newObject();
		ArrayNode results = message.putArray("result");
		Logger.info(String.valueOf(searchResult.size()) + " found");

		for (ArrayList<String> result : searchResult) {
			ObjectNode innerMsg = Json.newObject();
			innerMsg.put("file", result.get(0));
			innerMsg.put("link", "Download/" + result.get(1));
			innerMsg.put("size", result.get(2));
			innerMsg.put("context", ctxEx.getContext(result.get(3), pattern));

			ArrayNode tags = message.putArray("tags");

			int tagcount = result.size() - 4;
			for (int tagnr = 0; tagnr < tagcount; tagnr++) {
				tags.add(result.get(4 + tagnr));
			}
			innerMsg.put("tags", tags);

			results.add(innerMsg);
		}
		socketOut.write(message);
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

	private ArrayList<ArrayList<String>> search(String pattern) {
		ArrayList<ArrayList<String>> searchResult = elasticServer.elasticSearch.search(elasticServer.client, pattern,
				"documents", "file");
		return searchResult;
	}

	@Override
	public void onReceive(Object arg0) throws Exception { // msg od innych
															// aktorów /
															// systemu

	}
}
