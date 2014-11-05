package controllers;

import java.io.File;
import java.io.IOException;

import model.ElasticSearchManager;
import model.ElasticSearchServer;
import model.FileParser;

import com.google.common.io.Files;

import play.*;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import views.html.*;

public class Application extends Controller {

    private static String dirPath = "files/";
    private static FileParser fp = new FileParser();
    private static ElasticSearchServer elasticServer = new ElasticSearchServer();
    private static ElasticSearchManager elasticSearch = new ElasticSearchManager();    
    
	public static Result index() {
        return ok(index.render("Your new application is ready."));
    }
    
    public static Result upload(){
    	MultipartFormData body = request().body().asMultipartFormData();
		FilePart uploadedFile = body.getFile("file");
		if (uploadedFile != null) {
			String fileName = uploadedFile.getFilename();
			String contentType = uploadedFile.getContentType(); 
			System.out.println("file type:"+contentType);
			File file = uploadedFile.getFile();

			String tmpPath=dirPath + fileName;
			
			try {
				Files.move(file, new File(tmpPath));
				System.out.println("saved in:"+tmpPath);
			} catch (IOException e) {
				System.out.println("save failed");
				e.printStackTrace();
			}

//			fp.parseFile(file);

			return ok(index.render("Plik "+fileName+" przesłano na serwer"));
		} else {
			flash("error", "Missing file");
			return redirect(routes.Application.index());    
		}
    }

}
