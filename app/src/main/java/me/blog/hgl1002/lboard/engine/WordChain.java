package me.blog.hgl1002.lboard.engine;

public class WordChain {

	public static final int DEFAULT_LENGTH = 3;

	public static final Word START = new Word("START", null);
	public static final Word END = new Word("END", null);

	Word[] words;

	public WordChain(Word[] words) {
		this.words = words;
	}

	public WordChain(int length) {
		this.words = new Word[length];
	}

	public int size() {
		return words.length;
	}

	public Word get(int position) {
		try {
			return words[position];
		} catch(ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	public Word[] getAll() {
		return words;
	}

}
