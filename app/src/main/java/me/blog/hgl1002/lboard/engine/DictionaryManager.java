package me.blog.hgl1002.lboard.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DictionaryManager {

	Map<String, LBoardDictionary> dictionaries;

	List<DictionaryListener> listeners;

	public DictionaryManager() {
		dictionaries = new HashMap<>();
		listeners = new ArrayList<>();
	}

	Thread workingThread;

	public boolean searchCurrentWord(final String dictionaryName, final int operation, final int order, final String keyString) {
		if(workingThread != null) {
			if(workingThread.isAlive()) return false;
		}
		workingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Word[] result = dictionaries.get(dictionaryName).searchCurrentWord(operation, order, keyString);
				for(DictionaryListener listener : listeners) {
					listener.onCurrentWord(dictionaryName, result);
				}
			}
		});
		workingThread.start();
		return true;
	}

	public boolean searchNextWord(final String dictionaryName, final int operation, final int order, final String keyString, final Word[] previousWords) {
		if(workingThread != null) {
			if(workingThread.isAlive()) return false;
		}
		workingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Word[] result = dictionaries.get(dictionaryName).searchNextWord(operation, order, keyString, previousWords);
				for(DictionaryListener listener : listeners) {
					listener.onNextWord(dictionaryName, result);
				}
			}
		});
		workingThread.start();
		return true;
	}

	public boolean searchNextWord(final String dictionaryName, final int operation, final int order, final String keyString, final Word[][] previousWords) {
		if(workingThread != null) {
			if(workingThread.isAlive()) return false;
		}
		workingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				List<Word> words = new ArrayList<>();
				for(int i = 0 ; i < previousWords.length ; i++) {
					words.addAll(Arrays.asList(dictionaries.get(dictionaryName).searchNextWord(operation, order, keyString, previousWords[i])));
				}
				Word[] result = new Word[words.size()];
				result = words.toArray(result);
				for(DictionaryListener listener : listeners) {
					listener.onNextWord(dictionaryName, result);
				}
			}
		});
		workingThread.start();
		return true;
	}

	public void addDictionary(String name, LBoardDictionary dictionary) {
		dictionaries.put(name, dictionary);
	}

	public LBoardDictionary getDictionary(String name) {
		return dictionaries.get(name);
	}

	public void removeDictionary(String name) {
		dictionaries.remove(name);
	}

	public void removeDictionary(LBoardDictionary dictionary) {
		dictionaries.remove(dictionary);
	}

	public void addListener(DictionaryListener listener) {
		listeners.add(listener);
	}

	public void removeListener(DictionaryListener listener) {
		listeners.remove(listener);
	}

	public interface DictionaryListener {
		void onNextWord(String dictionaryName, Word[] words);
		void onCurrentWord(String dictionaryName, Word[] words);
	}

}
