package me.blog.hgl1002.lboard.engine;

public interface WritableDictionary extends LBoardDictionary {

	public int learnWord(Word word);

	public int learnWords(Word[] words);

}
