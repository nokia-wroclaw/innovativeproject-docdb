package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import model.ContextExtractor;

import org.junit.Before;
import org.junit.Test;

public class ContextExtractorTest {

	private ContextExtractor ctxEx;
	private String content;
	private String tagsPattern;
	private String noTag;
	private ArrayList<String> tagList;

	@Before
	public void setUp() throws Exception {
		ctxEx = ContextExtractor.getInstance();
		content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod "
				+ "tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim "
				+ "veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea "
				+ "commodo consequat. Duis aute irure dolor in reprehenderit in voluptate "
				+ "velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat "
				+ "cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id " + "est laborum.";

		tagsPattern = "#tojestTag Witam. Szukam czegos #drugitag z tagami #trzeci";
		noTag = "Witam. Szukam czegos z tagami";
		tagList = new ArrayList<>();
		tagList.add("tojestTag");
		tagList.add("drugitag");
		tagList.add("trzeci");
	}

	@Test
	public void testGetContext() {
		String ctx;
		ctx = ctxEx.getContext(content, "Lorem");// 50
		assertEquals(193, ctx.length());
		ctx = ctxEx.getContext(content, "enim");// ind 5
		assertEquals(7, ctx.indexOf("enim"));
		ctx = ctxEx.getContext(content, "laborum.");// <60
		assertTrue(String.valueOf(ctx.length()), ctx.length() < 120);
		ctx = ctxEx.getContext(content, "nostrud");// >70
		assertTrue(String.valueOf(ctx.length()), ctx.length() > 70);
		ctx = ctxEx.getContext(content, "ullamco");// 100
		assertEquals(256, ctx.length());
		ctx = ctxEx.getContext(content, "dgsdhfghfdhdfhfgh");// 100 pierwszych
																// znaków
		assertEquals(200, ctx.length());
		assertEquals(0, ctx.indexOf("Lorem"));

	}

	@Test
	public void testStripTags() {
		String ctx;
		ctx = ctxEx.stripTags(tagsPattern);
		assertEquals(noTag, ctx);
	}

	@Test
	public void testExtractTags() {
		List<String> ctx;
		ctx = ctxEx.extractTags(tagsPattern);
		assertEquals(tagList, ctx);
	}

}
