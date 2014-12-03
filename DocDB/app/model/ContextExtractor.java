package model;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContextExtractor {
	private static ContextExtractor instance = null;
	private Pattern tagPattern;
	private String patternString;

	private ContextExtractor() {
		patternString = "(#\\S+\\s)|(\\s#\\S+)";
		tagPattern = Pattern.compile(patternString);
	}

	/**
	 * returns search pattern without tags
	 */
	public String stripTags(String pattern) {
		return pattern.replaceAll(patternString, "");
	}

	/**
	 * returns list of tags in pattern
	 */
	public List<String> extractTags(String pattern) {
		List<String> tags = new LinkedList<>();
		Matcher m = tagPattern.matcher(pattern);
		while (m.find()) {
			tags.add(m.group().trim().substring(1));
		}
		return tags;
	}

	/**
	 * returns context of given word within given content
	 */
	public String getContext(String content, String word) {
		int distanceFromWord = 200;
		int wordStart = content.indexOf(word);
		if (wordStart == -1)
			return content.substring(0, distanceFromWord >= content.length() ? content.length() : distanceFromWord);
		int ctxStart = wordStart - distanceFromWord <= 0 ? 0 : wordStart - distanceFromWord;
		int ctxEnd = wordStart + distanceFromWord >= content.length() ? content.length() : wordStart + distanceFromWord;
		String context = content.substring(ctxStart, ctxEnd);
		int dotStart;
		if ((dotStart = context.substring(0, wordStart - ctxStart).indexOf(".")) != -1) {
			context = context.substring(dotStart + 1);
		}
		context = context.replaceAll("\n", " ");// usuwanie nowych linii
		ctxStart = context.indexOf(" ");// od spacji
		ctxEnd = context.lastIndexOf(" ");// do spacji
		if (ctxEnd < context.lastIndexOf("."))
			ctxEnd = context.lastIndexOf(".");// lub do kropki
		context = context.substring(ctxStart, ctxEnd);
		context = context.replaceAll(word, "<b>" + word + "</b>");
		return context;
	}

	public static ContextExtractor getInstance() {
		if (instance == null)
			instance = new ContextExtractor();
		return instance;
	}
}
