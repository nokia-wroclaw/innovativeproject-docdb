package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import model.ClientWebSocket;
import model.ElasticSearchServer;
import model.FileHandler;

import org.apache.commons.lang.RandomStringUtils;

import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
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
	private static FileHandler fileHandler = new FileHandler(elasticServer);
	private static Random rnd = new Random();

	public static Result index() {
		Logger.info("service started");
		return ok(index.render(""));
	}

	public static Result uploadLink(String tags) {
		DynamicForm requestData = Form.form().bindFromRequest();
		String link = requestData.get("link");
		String filename = getFreeRandomFileName() + ".html";
		try {
			File uploadedLink = saveLinkToFile(link, filename);

			Logger.info("link received. tags are:\"" + tags + "\". Handling...");

			ArrayList<String> tagList = new ArrayList<>(Arrays.asList(tags.split(",")));

			fileHandler.handleFile(uploadedLink, tagList);

			return ok(index.render("Link is on server"));
		} catch (IOException e) {
			e.printStackTrace();
			return internalServerError("Oops, link cannot be handled!");
		}
	}

	public static Result upload(String tags) {
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart uploadedFile = body.getFile("file");
		if (uploadedFile != null) {
			Logger.info("file received. tags are:\"" + tags + "\". Handling...");

			ArrayList<String> tagList = new ArrayList<>(Arrays.asList(tags.split(",")));

			fileHandler.handleFile(uploadedFile, tagList);

			return ok(index.render("File is on server"));
		} else {
			flash("error", "Missing file");
			return redirect(routes.Application.index());
		}
	}

	public static Result getFile(String path) {
		File file = new File("files/" + path);
		if (file.exists()) {

			response().setHeader("Content-Type", file.getName());
	        response().setHeader("Content-Length", String.valueOf(file.length()));
	        response().setHeader("Content-Disposition", "inline; filename=" + path);

			return ok(file);
		} else {
			return redirect(routes.Application.index());
		}
	}

	private static String getRandomName() {
		return RandomStringUtils.randomAlphanumeric(20);
	}

	private static String getFreeRandomFileName() {
		String filename;
		do {
			filename = getRandomName();
		} while (new File(filename).exists());
		return filename;
	}

	/**
	 * @param link
	 * @param filename
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private static File saveLinkToFile(String link, String filename) throws MalformedURLException, IOException {
		URL url = new URL(link);
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		PrintWriter printWriter = new PrintWriter(new FileWriter(filename));
		String line;
		while ((line = reader.readLine()) != null) {
			printWriter.println(line);
		}
		reader.close();
		printWriter.close();
		return new File(filename);
	}

	// websocket

	public static Result SearchScript() {
		return ok(SearchScript.render());
	}

	public static WebSocket<JsonNode> WebSocket() {
		return new WebSocket<JsonNode>() {

			@Override
			public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {

				// Starting webSocket handler
				Akka.system().actorOf(Props.create(ClientWebSocket.class, in, out, elasticServer));
			}

		};
	}
}
