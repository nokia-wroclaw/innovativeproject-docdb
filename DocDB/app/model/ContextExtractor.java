package model;

import play.Logger;

public class ContextExtractor {
	private static ContextExtractor instance = null;

	private ContextExtractor() {

	}

	public String getContext(String content, String word) {
		int distanceFromWord = 200;
		int wordStart = content.indexOf(word);
		if (wordStart == -1)
			return content.substring(0,
					distanceFromWord >= content.length() ? content.length()
							: distanceFromWord);
		int ctxStart = wordStart - distanceFromWord <= 0 ? 0 : wordStart
				- distanceFromWord;
		int ctxEnd = wordStart + distanceFromWord >= content.length() ? content
				.length() : wordStart + distanceFromWord;
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
