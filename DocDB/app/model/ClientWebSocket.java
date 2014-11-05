package model;

import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.WebSocket;
import akka.actor.UntypedActor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ClientWebSocket extends UntypedActor{
	
	private WebSocket.In<JsonNode> in;
	private WebSocket.Out<JsonNode> out;
	private String username;
	private ElasticSearchManager searchMan;
	private ElasticSearchServer elasticServer;
	
	ClientWebSocket(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out, String username, ElasticSearchManager searchMan, ElasticSearchServer elasticServer){
		System.out.println("New ClientWebSocket");
		this.in = in;
		this.out = out;
		this.username = username;
		this.searchMan = searchMan;
		this.elasticServer = elasticServer;
		
		this.in.onMessage(new Callback<JsonNode>() {						//msg z socketu
			public void invoke(JsonNode event) {
				checkEvent(event);
			} 
		});

		this.in.onClose(new Callback0() {								//socket się zamknął
			public void invoke() {
				//stop actor
				getContext().stop(getSelf());
			}
		});
	}


	protected void checkEvent(JsonNode event) {						//obsługa eventów z websocketa
		ObjectNode message = Json.newObject();

		if(event.has("request")){
			String request = event.get("request").asText();
			StringBuilder sb = new StringBuilder();

			if(request.equals("search")){

				String pattern = event.get("pattern").asText();

				String[][] searchResult = searchMan.search(elasticServer.client, pattern, "twitter", "tweet");


				for(int i = 0 ; i < searchResult.length ; i ++){
					sb.append("<span class=\"fileLine\">"
						+ searchResult[i][0]
						+ searchResult[i][1]
						+ searchResult[i][2]
						+ searchResult[i][3]
						+ "</span><br>");
				}
			}
			message.put("result", sb.toString());
		}
		out.write(message);				//odpowiedz do websocketa (klienta)
	}

	
	
	@Override
	public void onReceive(Object arg0) throws Exception { // msg od innych aktorów / systemu
		
	}
}
