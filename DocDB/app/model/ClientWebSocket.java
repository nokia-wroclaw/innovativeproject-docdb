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
	private ContextExtractor ctxEx;
	
	ClientWebSocket(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out, ElasticSearchManager searchMan, ElasticSearchServer elasticServer){
		Logger.info("New ClientWebSocket");
		this.in = in;
		this.out = out;
		this.searchMan = searchMan;
		this.elasticServer = elasticServer;
		ctxEx = ContextExtractor.getInstance();
		
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
								+"\", link:\"Download/"
								+ searchResult.get(i).get(1)//get link to file (routes)
								+"\", size:\""
								+ searchResult.get(i).get(2)//get file size
								+"\", context:\""
								+ ctxEx.getContext(searchResult.get(i).get(3),pattern)
								+"\", tags:\"");
								int tagcount = searchResult.get(i).size() - 4;
								for(int tagnr=0 ; tagnr<tagcount ; tagnr++){
									sb.append(searchResult.get(i).get(4)
											+", ");
								}
								sb.append("\"}");
						/*
						 * searchResult.get(i).get(3) - content
						 * searchResult.get(i).get(4) - first tag, and so on
						 * quantity of tags searchResult.get(i).size() - 4
						 * */
						if (i< searchResult.size()-1){
							sb.append(",");
						}
					}
					sb.append("]");
					String sbResult = sb.toString();
					Logger.info(sbResult);
					message.put("result", sbResult);
				}
			}
		}
		out.write(message);				//odpowiedz do websocketa (klienta)
	}

	
	
	@Override
	public void onReceive(Object arg0) throws Exception { // msg od innych aktorów / systemu
		
	}
}
