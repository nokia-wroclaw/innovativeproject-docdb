package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.ClientWebSocket;
import model.ElasticSearchManager;
import model.ElasticSearchServer;
import model.FileHandler;
import play.Logger;
import play.libs.Akka;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.index;
import views.js.SearchScript;
import akka.actor.Props;

import com.fasterxml.jackson.databind.JsonNode;

public class Application extends Controller {
    private static ElasticSearchServer elasticServer = new ElasticSearchServer();
    private static ElasticSearchManager elasticSearch = new ElasticSearchManager(); 
    private static FileHandler fileHandler = new FileHandler(elasticServer);   
    
	public static Result index() {
        Logger.info("service started");
		return ok(index.render("Your new application is ready."));
    }
	

    public static Result upload(){
			return upload("");
    }
    
    public static Result upload(String tags){
    	MultipartFormData body = request().body().asMultipartFormData();
		FilePart uploadedFile = body.getFile("file");
		if (uploadedFile != null) {
			Logger.info("file received. tags are:\""+tags+"\". Handling...");

			String[] tagArray = tags.split(",");
			ArrayList <String> tagList = new ArrayList<String>(Arrays.asList(tagArray));
			fileHandler.handleFile(uploadedFile, tagList);
			
			return ok(index.render("Plik przeslano na serwer"));
		} else {
			flash("error", "Missing file");
			return redirect(routes.Application.index());    
		}
    }


	public static Result getFile(String path) {
		File file = new File("files/"+path);
		if(file.exists()){
			response().setContentType("application/x-download");
			response().setHeader("Content-disposition","attachment; filename="+path);
			return ok(file);
		}else{
			return redirect(routes.Application.index());   
		}
	}

	//websocket


	public static Result SearchScript() {
		return ok(SearchScript.render());
	}
	
	public static WebSocket<JsonNode> WebSocket() {
		return new WebSocket<JsonNode>() {

			public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
				
				//Starting webSocket handler
				Akka.system().actorOf(Props.create(ClientWebSocket.class, in, out, elasticSearch, elasticServer));
			}

		};
	}
}
