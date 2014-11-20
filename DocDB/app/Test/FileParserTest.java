import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.junit.BeforeClass;


public class FileParserTest {

	static File f;
	static File f_2;
	static FileParser fp;
	static String [] testArray;
	
	@BeforeClass
	public static void setUp() {
		f = new File ("Wstep.pdf");
		fp = new FileParser();
		testArray = fp.parseFile(f);
		
	}
	
	@Test
	public static void titleNotFoundTest() {
		
		assertFalse(f.getName().equals(testArray[0]));
	}
	
	@Test
	public static void authorNotFoundTest() {
		assertTrue(testArray[1].equals("No_author"));
	}
	
	@Test
	public static void sizeOfFileTest() {
		assertTrue(new Integer(1771).equals(Integer.valueOf(testArray[4])));
	}
	
	@Test
	public static void titleFoundTest() {
		
		assertTrue(f_2.getName().equals(testArray[0]));
	}
	
	@Test
	public static void authorFoundTest() {
		assertFalse(testArray[1].equals("No_author"));
	}
	
	

}
