import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.junit.BeforeClass;


public class FileParserTest {

	static File f;
	static File f_2;
	static FileParser fp;
	static String [] testArrayWstep;
	static String [] testArrayAnaliza;
	
	@BeforeClass
	public static void setUp() {
		f = new File ("Wstep.pdf");
		f_2 = new File ("AnalizaNumeryczna.pdf")
		fp = new FileParser();
		testArrayWstep = fp.parseFile(f);
		testArrayAnaliza = fp.parseFile(f_2);
		
	}
	
	@Test
	public static void titleNotFoundTest() {
		
		assertFalse(f.getName().equals(testArrayWstep[0]));
	}
	
	@Test
	public static void authorNotFoundTest() {
		assertTrue(testArrayWstep[1].equals("No_author"));
	}
	
	@Test
	public static void sizeOfFileTest() {
		assertTrue(new Integer(1771).equals(Integer.valueOf(testArrayWstep[4])));
	}
	
	@Test
	public static void titleFoundTest() {
		
		assertTrue(f_2.getName().equals(testArrayAnaliza[0]));
	}
	
	@Test
	public static void authorFoundTest() {
		assertFalse(testArrayAnaliza[1].equals("No_author"));
	}
	
	

}
