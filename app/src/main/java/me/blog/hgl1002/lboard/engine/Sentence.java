package me.blog.hgl1002.lboard.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Sentence extends  Word {

	List<Word> words;

	Sentence prev;

	public Sentence(Sentence prev, String input, List<Word> words) {
		this(input, words);
		this.prev = prev;
	}

	public Sentence(String input, List<Word> words) {
		if(words == null || words.isEmpty()) {
			this.words = new ArrayList<>();
			this.candidate = "";
			this.stroke = "";
			this.frequency = 0;
		} else {
			this.words = words;
			Word first = words.get(0);
			if(words.size() == 1) {
				this.candidate = first.getCandidate();
				this.stroke = first.getStroke();
				this.frequency = first.getFrequency();
				this.attribute = first.getAttribute();
			} else {
				this.candidate = candidate.toString();
				this.stroke = input;
			}
		}
	}

	public void append(Word word) {
		if(word == null) return;
		words.add(word);
	}

	public void remove(int index) {
		words.remove(index);
	}

	public void removeLast() {
		words.remove(words.size() - 1);
	}

	public Word get(int index) {
		return words.get(index);
	}

	public Word getLast() {
		return words.get(words.size() - 1);
	}

	public Word pop() {
		Word word = getLast();
		removeLast();
		return word;
	}

	public int size() {
		return words.size();
	}

	public List<Word> getAll() {
		return words;
	}

	public Sentence getPrev() {
		return prev;
	}

	public void setPrev(Sentence prev) {
		this.prev = prev;
	}

	@Override
	public String getCandidate() {
		StringBuffer candidate = new StringBuffer();
		for(Word word : words) {
			candidate.append(word.getCandidate() + " ");
		}
		if(candidate.length() > 0) candidate.deleteCharAt(candidate.length() - 1);
		this.candidate = candidate.toString();
		return this.candidate;
	}

	@Override
	public String getStroke() {
		StringBuffer stroke = new StringBuffer();
		for(Word word : words) {
			stroke.append(word.getStroke() + " ");
		}
		if(stroke.length() > 0) stroke.deleteCharAt(stroke.length() - 1);
		this.stroke = stroke.toString();
		return this.stroke;
	}
}
