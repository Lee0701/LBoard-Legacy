package me.blog.hgl1002.lboard.engine;

public interface LBoardDictionary {

	public static final int ORDER_BY_FREQUENCY = 0;
	public static final int ORDER_BY_KEY = 1;

	public static final int SEARCH_EXACT = 0;
	public static final int SEARCH_PREFIX = 1;
	public static final int SEARCH_CHAIN = 2;

	public Word[] searchCurrentWord(int operation, int order, String keyString);
	public Word[] searchNextWord(int operation, int order, String keyString, Word[] previousWords);

}
