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

public class ClientWebSocket extends UntypedActor{
	
	private WebSocket.In<JsonNode> in;
	private WebSocket.Out<JsonNode> out;
	private ElasticSearchManager searchMan;
	private ElasticSearchServer elasticServer;
	
	ClientWebSocket(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out, ElasticSearchManager searchMan, ElasticSearchServer elasticServer){
		Logger.info("New ClientWebSocket");
		this.in = in;
		this.out = out;
		this.searchMan = searchMan;
		this.elasticServer = elasticServer;
		
		this.in.onMessage(new Callback<JsonNode>() {						//msg z socketu
			public void invoke(JsonNode event) {
				checkEvent(event);
			} 
		});

		this.in.onClose(new Callback0() {								//socket sie zamkna
			public void invoke() {
				//stop actor
				getContext().stop(getSelf());
			}
		});
	}


	protected void checkEvent(JsonNode event) {						//obslug eventow z websocketa
		ObjectNode message = Json.newObject();

		if(event.has("request")){
			String request = event.get("request").asText();
			StringBuilder sb = new StringBuilder();

			if(request.equals("search")){

				String pattern = event.get("pattern").asText();
				Logger.info("searching for:"+pattern);
				ArrayList<ArrayList<String>> searchResult = searchMan.search(elasticServer.client, pattern, "twitter", "tweet");
				if (searchResult==null){
					Logger.info("No results");
					message.put("result", "{}");
				}else{
					Logger.info(String.valueOf(searchResult.size())+" found");

					sb.append("[");
					for(int i = 0 ; i < searchResult.size() ; i ++){
						sb.append("{file:\""
								+ searchResult.get(i).get(0)//get file name
								+"\", size:\""
								+ searchResult.get(i).get(2)//get file size
								+"\", link:\"Download/"
								+ searchResult.get(i).get(1)//get link to file (routes)
								+ "\"}");
						/*
						 * searchResult.get(i).get(3) - content
						 * searchResult.get(i).get(4) - first tag, and so on
						 * quantity of tags searchResult.get(i).size() - 4
						 * */
						//System.out.println(searchResult.get(i));
						if (i< searchResult.size()-1){
							sb.append(",");
						}
					}
					sb.append("]");
					message.put("result", sb.toString());
				}
			}
		}
		out.write(message);				//odpowiedz do websocketa (klienta)
	}

	
	
	@Override
	public void onReceive(Object arg0) throws Exception { // msg od innych aktorów / systemu
		
	}
}
