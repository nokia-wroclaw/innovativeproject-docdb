package model;

import java.util.ArrayList;

import play.Logger;
import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.WebSocket;
import akka.actor.UntypedActor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ClientWebSocket extends UntypedActor {

	private final WebSocket.In<JsonNode> socketIn;
	private final WebSocket.Out<JsonNode> socketOut;
	private final ElasticSearchServer elasticServer;
	private final ContextExtractor ctxEx;

	ClientWebSocket(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out,
			ElasticSearchServer elasticServer) {
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

		StringBuilder sb = new StringBuilder();

		String pattern = event.get("pattern").asText();
		Logger.info("searching for:" + pattern);

		ArrayList<ArrayList<String>> searchResult = search(pattern);
		if (searchResult == null) {
			ObjectNode message = Json.newObject();
			message.put("result", "{}");
			Logger.info("No results");
			socketOut.write(message);
			return;
		}

		ObjectNode message = Json.newObject();
		Logger.info(String.valueOf(searchResult.size()) + " found");

		for (ArrayList<String> result : searchResult) {
			ObjectNode innerMsg = Json.newObject();
			innerMsg.put("file", result.get(0));
			innerMsg.put("link", "Download/" + result.get(1));
			innerMsg.put("size", result.get(2));
			innerMsg.put("context", ctxEx.getContext(result.get(3), pattern));

			int tagcount = result.size() - 4;
			for (int tagnr = 0; tagnr < tagcount; tagnr++) {
				sb.append(searchResult.get(i).get(4 + tagnr) + ", ");
			}
			sb.append("\"}");

			innerMsg.put("tags", tags);

		}

		sb.append("[");

		for (int i = 0; i < searchResult.size(); i++) {

			sb.append("{file:\""
					+ searchResult.get(i).get(0)// get file name
					+ "\", link:\"Download/"
					+ searchResult.get(i).get(1)// get link to file
												// (routes)
					+ "\", size:\""
					+ searchResult.get(i).get(2)// get file size
					+ "\", context:\""
					+ ctxEx.getContext(searchResult.get(i).get(3), pattern)
					+ "\", tags:\"");

			int tagcount = searchResult.get(i).size() - 4;
			for (int tagnr = 0; tagnr < tagcount; tagnr++) {
				sb.append(searchResult.get(i).get(4 + tagnr) + ", ");
			}
			sb.append("\"}");
			/*
			 * searchResult.get(i).get(3) - content searchResult.get(i).get(4) -
			 * first tag, and so on quantity of tags searchResult.get(i).size()
			 * - 4
			 */
			if (i < searchResult.size() - 1) {
				sb.append(",");
			}
		}
		sb.append("]");
		String sbResult = sb.toString();
		// Logger.info(sbResult);
		message.put("result", sbResult);
		socketOut.write(message); // odpowiedz do websocketa (klienta)
	}

	private ArrayList<ArrayList<String>> search(String pattern) {
		ArrayList<ArrayList<String>> searchResult = elasticServer.elasticSearch
				.search(elasticServer.client, pattern, "twitter", "tweet");
		return searchResult;
	}

	@Override
	public void onReceive(Object arg0) throws Exception { // msg od innych
															// aktorów /
															// systemu

	}
}
