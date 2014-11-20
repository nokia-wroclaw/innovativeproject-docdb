package Test;

import static org.junit.Assert.*;

import java.util.Map;

import model.*;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ElasticSearchManagerTest {

	static ElasticSearchServer server;
	static ElasticSearchManager manager;

	@BeforeClass
	public static void setUp() {

		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO); // wyswietli tylko info
		server = new ElasticSearchServer();
		manager = new ElasticSearchManager();
	}

	@AfterClass
	public static void tearDown() {

		server.closeNode();
	}

	@Before
	public void insertTest() {

		try {
			String[] parsedFile = { "title", "testFile",
					"there is nothing interesting here!", "", "156" };
			Map json = manager.putJsonDocument(parsedFile);
			manager.insert(server.client, json, "test", "testing");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			fail("Failed to insert this to server");
		}

	}

	// case when nothing was found
	@Test
	public void SearchTestNull() {

		assertNull(manager.search(server.client, "content", "test", "testing"));
	}

	@Test
	public void SearchTest() {

		assertTrue(manager.search(server.client, "nothing", "test", "testing") != null);
	}
	
	@Test
	public void WrongDataSearchTest() {
		
		assertTrue(manager.search(server.client, "nothing", "foo", "foo") == null);
	}

}
