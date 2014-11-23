package Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import model.ContextExtractor;

import org.junit.Before;
import org.junit.Test;

public class ContextExtractorTest {

	private ContextExtractor ctxEx;
	private String content;

	@Before
	public void setUp() throws Exception {
		ctxEx = ContextExtractor.getInstance();
		content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod "
				+ "tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim "
				+ "veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea "
				+ "commodo consequat. Duis aute irure dolor in reprehenderit in voluptate "
				+ "velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat "
				+ "cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id "
				+ "est laborum.";
	}

	@Test
	public void testGetContext() {
		String ctx; 
		ctx = ctxEx.getContext(content, "Lorem");//50
		assertEquals(50, ctx.length());
		ctx = ctxEx.getContext(content, "enim");//ind 5
		assertEquals(5, ctx.indexOf("enim"));
		ctx = ctxEx.getContext(content, "laborum.");// <60
		assertTrue(String.valueOf(ctx.length()), ctx.length()<60);
		ctx = ctxEx.getContext(content, "nostrud");//>70
		assertTrue(String.valueOf(ctx.length()), ctx.length()>70);
		ctx = ctxEx.getContext(content, "ullamco");//100
		assertEquals(100, ctx.length());
		ctx = ctxEx.getContext(content, "dgsdhfghfdhdfhfgh");//100 pierwszych znaków
		assertEquals(100, ctx.length());
		assertEquals(0, ctx.indexOf("Lorem"));
		
	}

}
