package model;

import org.apache.log4j.BasicConfigurator;

import play.*;

public class Settings extends GlobalSettings {

  @Override
  public void onStart(Application app) {
	  BasicConfigurator.configure();
    Logger.info("Application has started");
  }  

  @Override
  public void onStop(Application app) {
    Logger.info("Application shutdown...");
  }  
}
