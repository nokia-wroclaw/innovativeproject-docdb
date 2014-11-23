package model;

public class ContextExtractor {
	private static ContextExtractor instance = null;
	
	private ContextExtractor(){
		
	}
	
	public String getContext(String content, String word){
		int wordStart = content.indexOf(word);
		if (wordStart==-1) return content.substring(0,100>=content.length()?content.length():100);
		int ctxStart = wordStart-50<0?0:wordStart-50;  
		int ctxEnd = wordStart+50>=content.length()?content.length():wordStart+50;
		String context = content.substring(ctxStart, ctxEnd);
		int dotStart;
		if((dotStart = context.substring(0, wordStart-ctxStart).indexOf("."))!=-1){
			context = context.substring(dotStart);
		}
		return context;
	}
	
	public static ContextExtractor getInstance(){
		if (instance==null) instance = new ContextExtractor();
		return instance;
	}
}
