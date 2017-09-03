package me.blog.hgl1002.lboard.engine;

public class WordChain {

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
