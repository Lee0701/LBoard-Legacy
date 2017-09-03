package me.blog.hgl1002.lboard.engine;

import android.view.KeyEvent;

import me.blog.hgl1002.lboard.LBoard;
import me.blog.hgl1002.lboard.cand.CandidatesViewManager;

public class LBoardPredictionEngine implements PredictionEngine {

	public static final char STROKE_SEPARATOR = '\t';

	protected LBoard parent;
	protected DictionaryManager dictionaryManager;

	protected PredictionEngineListener listener;

	protected String dictionaryName;

	private String composingChar;
	private String composingWord;
	private String composingCharStroke;
	private String composingWordStroke;
	private Sentence sentence;

	private boolean start;

	protected CandidatesViewManager.CandidatesViewListener candidatesViewListener
			= new CandidatesViewManager.CandidatesViewListener() {
		@Override
		public void onSelect(Object candidate) {
			if(candidate instanceof Word) {
				commitComposingChar();
				clearComposingWord();
				appendWord((Word) candidate);
				start = false;
				parent.updateInput();
				updatePrediction();
			}
		}
	};

	public LBoardPredictionEngine(LBoard parent) {
		this.parent = parent;
		dictionaryManager = new DictionaryManager();
	}

	@Override
	public void start(boolean restarting) {
		commitComposingChar();
		if(restarting) {
			if(!composingWord.isEmpty()) appendWord(composingWord, composingWordStroke);
			clearComposingWord();
			startNewSentence(sentence);
		} else {
			startNewSentence(null);
		}
		parent.updateInput();
		updatePrediction();
		start = true;
	}

	@Override
	public void composeChar(String composingChar) {
		this.composingChar = composingChar;
	}

	@Override
	public void commitComposingChar() {
		this.composingWord += composingChar;
		this.composingChar = "";

		if(composingCharStroke != "")
				this.composingWordStroke += String.valueOf(STROKE_SEPARATOR) + composingCharStroke;
		this.composingCharStroke = "";
	}

	@Override
	public boolean backspace() {
		if(parent.backspace()) {
			composingCharStroke = composingCharStroke.substring(0, composingCharStroke.length()-1);
			if(!composingCharStroke.isEmpty()) updateCandidates();
			else updatePrediction();
		} else {
			if(!composingWord.isEmpty()) {
				composingWord = composingWord.substring(0, composingWord.length()-1);
				for(int i = composingWordStroke.length()-1 ; i >= 0 ; i--) {
					if(composingWordStroke.charAt(i) == STROKE_SEPARATOR) {
						composingWordStroke = composingWordStroke.substring(0, i);
						break;
					}
				}
				updateCandidates();
			} else if(sentence.size() > 0) {
				Word last = sentence.getLast();
				if((last.getAttribute() & Sentence.ATTRIBUTE_SPACED) != 0) {
					last.setAttribute(last.getAttribute() & ~Sentence.ATTRIBUTE_SPACED);
				} else {
					sentence.pop();
				}
				clearComposingWord();
				updatePrediction();
			} else {
				clearComposingWord();
				updatePrediction();
				return false;
			}
		}
		parent.updateInput();
		return true;
	}

	@Override
	public void appendStroke(String stroke) {
		composingCharStroke += stroke;
	}

	@Override
	public void appendText(CharSequence text) {
		commitComposingChar();
		appendWord(composingWord, composingWordStroke);
		clearComposingWord();
		composingWord += text;
		composingWordStroke += String.valueOf(STROKE_SEPARATOR) + text;
		appendWord(composingWord, composingWordStroke);
		clearComposingWord();
		parent.updateInput();
		updatePrediction();
	}

	public void appendWord(String composingWord, String composingWordStroke) {
		this.appendWord(composingWord, composingWordStroke, 0);
	}

	public void appendWord(String composingWord, String composingWordStroke, int attribute) {
		String stroke = composingWordStroke.replaceAll(String.valueOf(STROKE_SEPARATOR), "");
		Word word = new Word(composingWord, stroke, 1, attribute);
		this.appendWord(word);
	}

	public void appendWord(Word word) {
		sentence.append(word);
	}

	@Override
	public void halfCommitWord() {
		commitComposingChar();
		if (composingWord.isEmpty() && sentence.size() > 0) {
			Word last = sentence.getLast();
			if ((last.getAttribute() & Sentence.ATTRIBUTE_SPACED) == 0) {
				last.setAttribute(last.getAttribute() | Sentence.ATTRIBUTE_SPACED);
			}
		} else if (!composingWord.isEmpty()) {
			appendWord(composingWord, composingWordStroke, Sentence.ATTRIBUTE_SPACED);
		}
		clearComposingWord();
		parent.updateInput();
		updatePrediction();
		start = false;
	}

	@Override
	public void commitSentence() {
		commitComposingChar();
		if (!composingWord.isEmpty()) appendWord(composingWord, composingWordStroke);
		clearComposingWord();
		boolean learn = parent.isPasswordField();
		if(learn) {
			learnSentence(sentence);
		}
		startNewSentence(sentence);
		updatePrediction();
		start = true;
	}

	public void clearComposingWord() {
		composingChar = "";
		composingCharStroke = "";
		composingWord = "";
		composingWordStroke = "";
	}

	public void startNewSentence(Sentence prev) {
		sentence = new Sentence(prev, null, null);
	}

	public void learnSentence(Sentence sentence) {
		learnWords(getWordChain(sentence, 0));
	}

	public void learnWord(Word word) {
		try {
			dictionaryManager.learnWord(dictionaryName, word);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void learnWords(Word[] words) {
		try {
			dictionaryManager.learnWords(dictionaryName, words);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updatePrediction() {
		Word[] chain = getWordChain(sentence, 0);
		if(start && chain[chain.length-1] != Word.START) {
			dictionaryManager.searchNextWord(
					dictionaryName,
					LBoardDictionary.SEARCH_CHAIN,
					LBoardDictionary.ORDER_BY_FREQUENCY,
					composingWord,
					chain);
		} else {
			dictionaryManager.searchNextWord(
					dictionaryName,
					LBoardDictionary.SEARCH_CHAIN,
					LBoardDictionary.ORDER_BY_FREQUENCY,
					composingWord,
					chain);
		}
	}

	public void updateCandidates() {
		String stroke = composingWordStroke + STROKE_SEPARATOR + composingCharStroke;
		stroke = stroke.replaceAll(String.valueOf(STROKE_SEPARATOR), "");
		dictionaryManager.searchCurrentWord(
				dictionaryName,
				LBoardDictionary.SEARCH_PREFIX,
				LBoardDictionary.ORDER_BY_FREQUENCY,
				stroke);
	}

	public static Word[] getWordChain(Sentence sentence, int position) {
		Word[] words = new Word[sentence.size() - position];
		for(int i = 0 ; i < words.length ; i++) {
			int index = words.length - i - 1;
			if(sentence.size()-1 - position - i < 0) {
				if(sentence.getPrev() != null && sentence.getPrev().size()-1 + sentence.size() - position - i >= 0) {
					Sentence prev = sentence.getPrev();
					words[index] = prev.get(prev.size()-1 + sentence.size() - position - i);
				} else {
					words[index] = Word.START;
				}
			} else {
				words[index] = sentence.get(sentence.size()-1 - position - i);
			}
		}
		return words;
	}

	@Override
	public String getComposing() {
		return sentence.getCandidate() + composingWord + composingChar;
	}

	@Override
	public void setListener(PredictionEngineListener listener) {
		this.listener = listener;
	}

	public CandidatesViewManager.CandidatesViewListener getCandidatesViewListener() {
		return candidatesViewListener;
	}

	public DictionaryManager getDictionaryManager() {
		return dictionaryManager;
	}

	public void setDictionaryManager(DictionaryManager dictionaryManager) {
		this.dictionaryManager = dictionaryManager;
	}

	public String getComposingChar() {
		return composingChar;
	}

	public void setComposingChar(String composingChar) {
		this.composingChar = composingChar;
	}

	public String getComposingWord() {
		return composingWord;
	}

	public void setComposingWord(String composingWord) {
		this.composingWord = composingWord;
	}

	public String getComposingCharStroke() {
		return composingCharStroke;
	}

	public void setComposingCharStroke(String composingCharStroke) {
		this.composingCharStroke = composingCharStroke;
	}

	public String getComposingWordStroke() {
		return composingWordStroke;
	}

	public void setComposingWordStroke(String composingWordStroke) {
		this.composingWordStroke = composingWordStroke;
	}

	public Sentence getSentence() {
		return sentence;
	}

	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
	}

	public boolean isStart() {
		return start;
	}

	public void setStart(boolean start) {
		this.start = start;
	}

	public String getDictionaryName() {
		return dictionaryName;
	}

	public void setDictionaryName(String dictionaryName) {
		this.dictionaryName = dictionaryName;
	}
}
