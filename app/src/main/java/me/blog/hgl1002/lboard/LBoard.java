package me.blog.hgl1002.lboard;

import android.Manifest;
import android.content.ClipDescription;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import me.blog.hgl1002.lboard.cand.CandidatesViewManager;
import me.blog.hgl1002.lboard.cand.TextCandidatesViewManager;
import me.blog.hgl1002.lboard.engine.DictionaryManager;
import me.blog.hgl1002.lboard.engine.LBoardDictionary;
import me.blog.hgl1002.lboard.engine.SQLiteDictionary;
import me.blog.hgl1002.lboard.engine.Sentence;
import me.blog.hgl1002.lboard.engine.Word;
import me.blog.hgl1002.lboard.engine.WordChain;
import me.blog.hgl1002.lboard.ime.LBoardInputMethod;
import me.blog.hgl1002.lboard.ime.charactergenerator.CharacterGenerator;
import me.blog.hgl1002.lboard.ime.charactergenerator.UnicodeCharacterGenerator;
import me.blog.hgl1002.lboard.ime.hardkeyboard.lhkb.LHKB1;
import me.blog.hgl1002.lboard.ime.softkeyboard.DefaultSoftKeyboard;

import me.blog.hgl1002.lboard.ime.KeyEventInfo;
import me.blog.hgl1002.lboard.ime.hardkeyboard.DefaultHardKeyboard;
import me.blog.hgl1002.lboard.search.DefaultSearchViewManager;
import me.blog.hgl1002.lboard.search.SearchEngine;
import me.blog.hgl1002.lboard.search.SearchViewManager;
import me.blog.hgl1002.lboard.search.data.ImageData;
import me.blog.hgl1002.lboard.search.data.UrlStringData;
import me.blog.hgl1002.lboard.search.engines.GoogleWebSearchEngine;

public class LBoard extends InputMethodService {

	public static final String DICTIONARY_KO = "ko";
	public static final String DICTIONARY_EN = "en";

	public static final int MSG_UPDATE_CANDIDATES = 1;
	public static final int MSG_UPDATE_PREDICTION = 2;

	public static final int DELAY_DISPLAY_CANDIDATES = 100;

	private static final CharacterStyle SPAN_COMPOSING_WORD = new BackgroundColorSpan(0xFF81DAF5);
	private static final CharacterStyle SPAN_COMPOSING_SENTENCE = new BackgroundColorSpan(0xFFE0F2F7);
	private static final CharacterStyle SPAN_COMPOSING_CHAR = new BackgroundColorSpan(0xFF00BFFF);

	protected static final List<String> SENTENCE_STOPS = new ArrayList<String>() {{
		add(".");
		add(",");
		add("?");
		add("!");
	}};

	protected ViewGroup mainInputView;
	protected View keyboardView;
	protected ViewGroup mainCandidatesView;
	protected View searchView;
	protected View candidatesView;

	protected SearchViewManager searchViewManager;

	protected DictionaryManager dictionaryManager;

	protected CandidatesViewManager candidatesViewManager;

	protected List<LBoardInputMethod> inputMethods;
	protected int currentInputMethodId;
	protected LBoardInputMethod currentInputMethod;

	protected Button searchButton;

	Animation slideUp, slideDown;

	private boolean inputted = false;
	private boolean searchViewShown = false;
	private String searchText = "", searchTextComposing = "";

	protected List<String> sentenceStops;

	private String composingChar;
	private String composingWord;
	private String composingCharStroke;
	private String composingWordStroke;
	private Stack<String> composingWordStrokeHistory = new Stack<>();
	private Sentence sentence;

	private boolean start;

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Word[] candidates;
			switch (msg.what) {
			case MSG_UPDATE_PREDICTION:
				candidatesViewManager.setCandidates((Word[]) msg.obj);
				break;

			case MSG_UPDATE_CANDIDATES:
				candidatesViewManager.setCandidates((Word[]) msg.obj);
				break;
			}
		}
	};

	protected DictionaryManager.DictionaryListener dictionaryListener = new DictionaryManager.DictionaryListener() {
		@Override
		public void onNextWord(String dictionaryName, Word[] words) {
			Message msg = handler.obtainMessage(MSG_UPDATE_PREDICTION);
			msg.obj = words;
			handler.sendMessageDelayed(msg, DELAY_DISPLAY_CANDIDATES);
		}

		@Override
		public void onCurrentWord(String dictionaryName, Word[] words) {
			Message msg = handler.obtainMessage(MSG_UPDATE_CANDIDATES);
			msg.obj = words;
			handler.sendMessageDelayed(msg, DELAY_DISPLAY_CANDIDATES);
		}
	};

	protected CharacterGenerator.CharacterGeneratorListener characterGeneratorListener
			= new CharacterGenerator.CharacterGeneratorListener() {
		@Override
		public void onCompose(CharacterGenerator source, String composing) {
			InputConnection ic = getCurrentInputConnection();
			if(ic == null) return;
			if(searchViewShown) {
				searchTextComposing = composing;
				searchViewManager.setText(searchText + searchTextComposing);
			} else {
				composeChar(composing);
				composingCharStroke = source.getStroke();
				updateInput();
				updateCandidates();
			}
		}

		@Override
		public void onCommit(CharacterGenerator source) {
			InputConnection ic = getCurrentInputConnection();
			if(ic == null) return;
			if(searchViewShown) {
				searchText += searchTextComposing;
				searchTextComposing = "";
			} else {
				commitComposingChar();
				updateInput();
			}
		}
	};

	protected CandidatesViewManager.CandidatesViewListener candidatesViewListener
			= new CandidatesViewManager.CandidatesViewListener() {
		@Override
		public void onSelect(Object candidate) {
			if(candidate instanceof Word) {
				commitComposingChar();
				clearComposing();
				appendWord((Word) candidate);
				updateInput();
				updatePrediction();
			}
		}
	};

	public LBoard() {
		inputMethods = new ArrayList<>();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		sentenceStops = SENTENCE_STOPS;

		slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
		slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);

		LBoardInputMethod sebeolFinal, qwerty;

		{
			DefaultSoftKeyboard softKeyboard = new DefaultSoftKeyboard(this);
			DefaultHardKeyboard hardKeyboard = new DefaultHardKeyboard(this);
			UnicodeCharacterGenerator generator = new UnicodeCharacterGenerator();
			generator.setListener(characterGeneratorListener);
			generator.setCombinationTable(UnicodeCharacterGenerator.loadCombinationTable(getResources().openRawResource(R.raw.comb_sebeol)));
			hardKeyboard.setCharacterGenerator(generator);
			hardKeyboard.setMappings(LHKB1.loadMappings(getResources().openRawResource(R.raw.layout_sebeol_final)));

			softKeyboard.createKeyboards(this, R.xml.keyboard_full_10cols, R.xml.keyboard_full_10cols, R.xml.keyboard_lower_default);
			CharSequence[][] labels = new CharSequence[0x100][2];
			for(int i = 0 ; i < labels.length ; i++) {
				for(int j = 0 ; j < labels[i].length ; j++) {
					long mapping = hardKeyboard.getMappings()[i][j];
					if(mapping != 0) labels[i][j] = new String(new char[] {(char) mapping});
					else labels[i][j] = null;
				}
			}
			softKeyboard.setLabels(labels);

			sebeolFinal = new LBoardInputMethod("Sebeolsik Final", softKeyboard, hardKeyboard, generator);
			sebeolFinal.setDictionaryName(DICTIONARY_KO);
		}

		{
			DefaultSoftKeyboard softKeyboard = new DefaultSoftKeyboard(this);
			DefaultHardKeyboard hardKeyboard = new DefaultHardKeyboard(this);
			CharacterGenerator generator = new UnicodeCharacterGenerator();
			generator.setListener(characterGeneratorListener);
			hardKeyboard.setMappings(LHKB1.loadMappings(getResources().openRawResource(R.raw.layout_qwerty)));

			softKeyboard.createKeyboards(this, R.xml.keyboard_qwerty_4rows, R.xml.keyboard_qwerty_4rows, R.xml.keyboard_lower_default);

			qwerty = new LBoardInputMethod("Qwerty", softKeyboard, hardKeyboard, generator);
			qwerty.setDictionaryName(DICTIONARY_EN);
		}

		inputMethods.add(qwerty);
		inputMethods.add(sebeolFinal);

		currentInputMethod = inputMethods.get(currentInputMethodId);

		SearchEngine engine = new GoogleWebSearchEngine();
		searchViewManager = new DefaultSearchViewManager(this, engine);

		dictionaryManager = new DictionaryManager();
		LBoardDictionary dictionary = new SQLiteDictionary(getFilesDir() + "/" + DICTIONARY_KO + ".dic");
		dictionaryManager.addDictionary(DICTIONARY_KO, dictionary);
		LBoardDictionary english = new SQLiteDictionary(getFilesDir() + "/" + DICTIONARY_EN + ".dic");
		dictionaryManager.addDictionary(DICTIONARY_EN, english);
		dictionaryManager.addListener(dictionaryListener);
		candidatesViewManager = new TextCandidatesViewManager();
		candidatesViewManager.setListener(candidatesViewListener);

		clearComposing();

		startNewSentence(null);

		updatePrediction();
	}

	@Override
	public View onCreateCandidatesView() {
		return super.onCreateCandidatesView();
	}

	@Override
	public View onCreateInputView() {
		mainInputView = (FrameLayout) getLayoutInflater().inflate(R.layout.main_input, null);
		LinearLayout linearLayout = (LinearLayout) mainInputView.findViewById(R.id.main_linear);
		searchButton = (Button) linearLayout.findViewById(R.id.search_button);

		updateInputView();

		searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchViewShown = !searchViewShown;
				if(searchViewShown) {
					candidatesView.setVisibility(View.INVISIBLE);
				} else {
					candidatesView.setVisibility(View.VISIBLE);
				}
			}
		});

		searchView = searchViewManager.createView(this);
		mainInputView.addView(searchView);

		candidatesView = candidatesViewManager.createView(this);
		mainInputView.addView(candidatesView);

		linearLayout.bringToFront();

		setCandidatesViewShown(true);

		return this.mainInputView;
	}

	public void updatePrediction() {
		WordChain chain = getWordChain(sentence, 0);
		if(start && chain.get(chain.size()-1) != WordChain.START) {
			Word[] start = new Word[] {WordChain.START, WordChain.START, WordChain.START};
			dictionaryManager.searchNextWord(
					currentInputMethod.getDictionaryName(),
					LBoardDictionary.SEARCH_CHAIN,
					LBoardDictionary.ORDER_BY_FREQUENCY,
					composingWord,
					new Word[][] {chain.getAll(), start});
		} else {
			dictionaryManager.searchNextWord(
					currentInputMethod.getDictionaryName(),
					LBoardDictionary.SEARCH_CHAIN,
					LBoardDictionary.ORDER_BY_FREQUENCY,
					composingWord,
					chain.getAll());
		}
	}

	public void updateCandidates() {
		String stroke = composingWordStroke + composingCharStroke;
		dictionaryManager.searchCurrentWord(
				currentInputMethod.getDictionaryName(),
				LBoardDictionary.SEARCH_PREFIX,
				LBoardDictionary.ORDER_BY_FREQUENCY,
				stroke);
	}

	public void updateInput() {
		composeSentence(sentence, composingWord, composingChar);
	}

	public void updateInputView() {
		FrameLayout placeholder = (FrameLayout) mainInputView.findViewById(R.id.keyboard_placeholder);
		placeholder.removeAllViews();
		keyboardView = currentInputMethod.getSoftKeyboard().createView(this);
		placeholder.addView(keyboardView);
	}

	@Override
	public void onStartInputView(EditorInfo info, boolean restarting) {
		super.onStartInputView(info, restarting);
		if(restarting) {
			commitComposingChar();
			if(!composingWord.isEmpty()) appendWord(composingWord, composingWordStroke, Word.ATTRIBUTE_SPACED);
			clearComposing();
			commitSentence(sentence, true, true);
			startNewSentence(sentence);
			updateInput();
			start = true;
			updatePrediction();
		} else {
			commitComposingChar();
			clearComposing();
			startNewSentence(sentence);
			updateInput();
			start = true;
			updatePrediction();
		}
	}

	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting);
		if(getCurrentInputConnection() != null) currentInputMethod.getCharacterGenerator().resetComposing();
	}

	public boolean onKeyEvent(KeyEvent event, boolean hardKey) {
		InputConnection ic = getCurrentInputConnection();
		boolean ret = false;
		switch(event.getKeyCode()) {
		case KeyEvent.KEYCODE_BACK:
			if(event.getAction() == KeyEvent.ACTION_UP) {
				if (searchViewShown) {
					hideSearchView();
				}
			}
			break;
		case -500:
			if(event.getAction() == KeyEvent.ACTION_DOWN) {
//				InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//				IBinder token = getWindow().getWindow().getAttributes().token;
//				if(inputted) {
//					manager.switchToLastInputMethod(token);
//				} else {
//					manager.switchToNextInputMethod(token, false);
//				}
				commitComposingChar();
				if(!composingWord.isEmpty()) {
					appendWord(composingWord, composingWordStroke, Word.ATTRIBUTE_SPACED);
					clearComposing();
				}

				if(++currentInputMethodId >= 2) currentInputMethodId = 0;
				currentInputMethod = inputMethods.get(currentInputMethodId);
				updateInputView();

				updateInput();
				start = true;
				updatePrediction();
			}
			return true;
		case KeyEvent.KEYCODE_DEL:
			if(event.getAction() == KeyEvent.ACTION_DOWN) {
				if (!currentInputMethod.getCharacterGenerator().backspace()) {
					if(searchViewShown) {
						if(searchText.length() > 0) {
							searchText = searchText.substring(0, searchText.length()-1);
						}
						searchViewManager.setText(searchText + searchTextComposing);
					} else {
						if(!composingWord.isEmpty()) {
							composingWord = composingWord.substring(0, composingWord.length()-1);
							if(composingWordStrokeHistory.isEmpty()) {
								composingWordStroke = "";
							} else {
								composingWordStroke = composingWordStrokeHistory.pop();
							}
							updateInput();
							updateCandidates();
						} else if(sentence.size() > 0) {
							Word last = sentence.getLast();
							if((last.getAttribute() & Sentence.ATTRIBUTE_SPACED) != 0) {
								last.setAttribute(last.getAttribute() & ~Sentence.ATTRIBUTE_SPACED);
							} else {
								sentence.pop();
							}
							clearComposing();
							updateInput();
							updateCandidates();
						} else {
							clearComposing();
							ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
							ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
						}
					}
				}
			} else {
				if(sentence.size() <= 0 && composingWord.isEmpty() && composingChar.isEmpty()) updatePrediction();
			}
			return true;
		}
		inputted = true;
		ret = currentInputMethod.getHardKeyboard().onKeyEvent(
				event, new KeyEventInfo.Builder().setKeyType(hardKey ? KeyEventInfo.KEYTYPE_HARDKEY : KeyEventInfo.KEYTYPE_SOFTKEY).build());

		if(event.getAction() == KeyEvent.ACTION_DOWN) {
			if (searchViewShown && !ret) {
				switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_ENTER:
					finishComposing();
					search();
					break;

				case KeyEvent.KEYCODE_SPACE:
					finishComposing();
					searchText += " ";
					break;
				}
				return true;
			}
		}

		if(event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_SPACE:
				commitComposingChar();
				if(composingWord.isEmpty() && sentence.size() > 0) {
					Word last = sentence.getLast();
					if((last.getAttribute() & Sentence.ATTRIBUTE_SPACED) == 0) {
						last.setAttribute(last.getAttribute() | Sentence.ATTRIBUTE_SPACED);
					}
				} else if(!composingWord.isEmpty()) {
					appendWord(composingWord, composingWordStroke, Sentence.ATTRIBUTE_SPACED);
				}
				clearComposing();
				updateInput();
				updatePrediction();
				start = false;
				return true;

			case KeyEvent.KEYCODE_ENTER:
				commitComposingChar();
				if(!composingWord.isEmpty()) appendWord(composingWord, composingWordStroke);
				clearComposing();
				boolean learn = !isPasswordField();
				commitSentence(sentence, learn);
				startNewSentence(sentence);
				updateInput();
				updatePrediction();
				start = true;
				break;

			}
		}
		if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == -114) {
			shareDictionary();
		}

		return ret;
	}

	public void shareDictionary() {
		try {
			FileInputStream fis = new FileInputStream(new File(getFilesDir(), DICTIONARY_KO + ".dic"));
			File file = new File("/storage/emulated/0/dictionary.dic");
			FileOutputStream fos = new FileOutputStream(file);
			byte[] data = new byte[fis.available()];
			fis.read(data);
			fos.write(data);
			shareImage("application/octet-stream", Uri.fromFile(file));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void resetSearch() {
		searchText = "";
		searchTextComposing = "";
		searchViewManager.setText("");
		searchViewManager.reset();
	}

	public void search() {
		searchViewManager.search();
		hideKeyboardView();
	}

	public void sendSearchResult(Object object) {
		InputConnection ic = getCurrentInputConnection();
		if(ic == null) {
			return;
		}
		if(object instanceof String) {
			ic.commitText((String) object, 1);
		} else if(object instanceof Bitmap) {
			Uri uri = getBitmapUri((Bitmap) object);
			if(uri !=  null) commitImage("image/png", uri, "");
		} else if(object instanceof UrlStringData) {
			UrlStringData data = (UrlStringData) object;
			ic.commitText(data.getDescription() + '\n' + data.getUrl(), 1);
		} else if(object instanceof ImageData) {
			ImageData data = (ImageData) object;
			Uri uri = null;
			if(data.getData() != null && data.getData() instanceof Bitmap) {
				uri = getBitmapUri((Bitmap) data.getData());
			} else {
				uri = getBitmapUri(data.getUrl());
			}
			if(uri != null) {
				String description = data.getDescription();
				if(description == null) description = "";
				commitImage("image/png", uri, description);
			} else {
				System.err.println("Image URI parse failed!");
			}
		}
	}

	public void hideKeyboardView() {
		keyboardView.startAnimation(slideDown);
		keyboardView.postOnAnimation(new Runnable() {
			@Override
			public void run() {
				keyboardView.setVisibility(View.INVISIBLE);
			}
		});
	}

	public void unhideKeyboardView() {
		candidatesView.setVisibility(View.VISIBLE);
		keyboardView.setVisibility(View.VISIBLE);
		searchButton.setVisibility(View.VISIBLE);
	}

	public void hideSearchView() {
		searchViewShown = false;
		unhideKeyboardView();
		resetSearch();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean result = onKeyEvent(event, true);
		if(!result) return super.onKeyDown(keyCode, event);
		else return result;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean result = onKeyEvent(event, true);
		if(!result) return super.onKeyUp(keyCode, event);
		else return result;
	}

	public void clearComposing() {
		composingWord = "";
		composingChar = "";
		composingWordStroke = "";
		composingCharStroke = "";
		composingWordStrokeHistory.clear();
	}

	public void commitText(CharSequence text) {
		if(searchViewShown) {
			searchText += text;
			searchViewManager.setText(searchText + searchTextComposing);
		} else {
			commitComposingChar();
			EditorInfo info = getCurrentInputEditorInfo();
			switch(info.inputType & EditorInfo.TYPE_MASK_CLASS) {
			case EditorInfo.TYPE_CLASS_TEXT:
				switch(info.inputType & EditorInfo.TYPE_MASK_VARIATION) {
				case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
				case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
				case EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD:
					getCurrentInputConnection().commitText(text, 1);
					break;

				default:
					if(sentenceStops.contains(text)) {
						commitComposingChar();
						appendWord(composingWord, composingWordStroke, 0);
						clearComposing();
						composingWord += text;
						composingWordStroke += text;
						appendWord(composingWord, composingWordStroke, 0);
						clearComposing();
						updateInput();
						start = true;
						updatePrediction();
					} else {
						composingWordStrokeHistory.push(composingWordStroke);
						composingWord += text;
						composingWordStroke += text;
						updateInput();
						updateCandidates();
					}
				}
				break;

			default:
				getCurrentInputConnection().commitText(text, 1);
			}
		}
	}

	public void composeChar(String composingChar) {
		this.composingChar = composingChar;
	}

	public void commitComposingChar() {
		this.composingWord += composingChar;
		this.composingChar = "";
		if(composingWordStrokeHistory.isEmpty() || !composingWordStrokeHistory.peek().equals(composingWordStroke)) {
			composingWordStrokeHistory.push(composingWordStroke);
		}

		this.composingWordStroke += composingCharStroke;
		this.composingCharStroke = "";
	}

	public void appendWord(String composingWord, String composingWordStroke) {
		this.appendWord(composingWord, composingWordStroke, 0);
	}

	public void appendWord(String composingWord, String composingWordStroke, int attribute) {
		Word word = new Word(composingWord, composingWordStroke, 1, attribute);
		this.appendWord(word);
	}

	public void appendWord(Word word) {
		sentence.append(word);
	}

	public void startNewSentence(Sentence prev) {
		sentence = new Sentence(prev, null, null);
	}

	public void composeSentence(Sentence sentence, String composingWord, String composingChar) {
		if(sentence == null) return;
		InputConnection ic = getCurrentInputConnection();
		SpannableStringBuilder str = new SpannableStringBuilder();
		str.append(sentence.getCandidate());
		int start = 0, end = str.length();
		str.setSpan(SPAN_COMPOSING_SENTENCE, start, end, Spanned.SPAN_COMPOSING);
		str.append(composingWord);
		start = end;
		end = str.length();
		str.setSpan(SPAN_COMPOSING_WORD, start, end, Spanned.SPAN_COMPOSING);
		str.append(composingChar);
		start = end;
		end = str.length();
		str.setSpan(SPAN_COMPOSING_CHAR, start, end, Spanned.SPAN_COMPOSING);
		ic.setComposingText(str, 1);
	}

	public void commitSentence(Sentence sentence, boolean learn) {
		this.commitSentence(sentence, learn, false);
	}

	public void commitSentence(Sentence sentence, boolean learn, boolean clear) {
		InputConnection ic = getCurrentInputConnection();

		ic.setComposingText("", 1);
		ic.finishComposingText();

		if(!clear) ic.commitText(sentence.getCandidate(), 1);

		if(learn) {
			for(int i = 0 ; i < sentence.size() ; i++) {
				WordChain chain = getWordChain(sentence, i);
				learnWordChain(chain);
				learnWord(sentence.get(i));
			}
		}

	}

	public void learnWord(Word word) {
		LBoardDictionary current = dictionaryManager.getDictionary(currentInputMethod.getDictionaryName());
		if(current instanceof SQLiteDictionary && word.getCandidate() != "") {
			SQLiteDictionary dictionary = (SQLiteDictionary) current;
			dictionary.learnWord(word);
		}
	}

	public void learnWordChain(WordChain prev) {
		LBoardDictionary current = dictionaryManager.getDictionary(currentInputMethod.getDictionaryName());
		if(current instanceof SQLiteDictionary) {
			SQLiteDictionary dictionary = (SQLiteDictionary) current;
			dictionary.learnChain(prev);
		}
	}

	public WordChain getWordChain(Sentence sentence, int position) {
		Word[] words = new Word[WordChain.DEFAULT_LENGTH];
		for(int i = 0 ; i < words.length ; i++) {
			int index = words.length - i - 1;
			if(sentence.size()-1 - position - i < 0) {
				if(sentence.getPrev() != null && sentence.getPrev().size()-1 + sentence.size() - position - i >= 0) {
					Sentence prev = sentence.getPrev();
					words[index] = prev.get(prev.size()-1 + sentence.size() - position - i);
				} else {
					words[index] = WordChain.START;
				}
			} else {
				words[index] = sentence.get(sentence.size()-1 - position - i);
			}
		}
		return new WordChain(words);
	}

	public void commitImage(String mimeType, Uri contentUri, String imageDescription) {
		String[] mimeTypes = EditorInfoCompat.getContentMimeTypes(getCurrentInputEditorInfo());
		boolean supported = false;
		for(String type : mimeTypes) {
			if(ClipDescription.compareMimeTypes(type, mimeType)) {
				supported = true;
			}
		}
		if(!supported) {
			System.err.println("Mime type " + mimeType + " is not supported!");
			shareImage(mimeType, contentUri);
			return;
		}
		InputContentInfoCompat inputContentInfo = new InputContentInfoCompat(
				contentUri,
				new ClipDescription(imageDescription, new String[] {mimeType}),
				null);
		InputConnection ic = getCurrentInputConnection();
		EditorInfo info = getCurrentInputEditorInfo();
		int flags = 0;
		if(Build.VERSION.SDK_INT >= 25) {
			flags |= InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION;
		}
		InputConnectionCompat.commitContent(
				ic, info, inputContentInfo, flags, null);
	}

	public void shareImage(String mimeType, Uri contentUri) {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType(mimeType);
		shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
		shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(shareIntent);
	}

	public void finishComposing() {
		currentInputMethod.getCharacterGenerator().resetComposing();
	}

	public Uri getBitmapUri(Bitmap bitmap) {
		if(!requestPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, "")) return null;
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
		String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "web_capture", null);
		return Uri.parse(path);
	}

	public Uri getBitmapUri(final String url) {
		if(!requestPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, "")) return null;
		final AtomicReference<Bitmap> bitmap = new AtomicReference<>();
		final AtomicBoolean exception = new AtomicBoolean();
		new Thread() {
			@Override
			public void run() {
				try {
					bitmap.set(BitmapFactory.decodeStream(new URL(url).openStream()));
				} catch(IOException e) {
					exception.set(true);
					e.printStackTrace();
				}
			}
		}.start();
		while(!exception.get() && bitmap.get() == null) {
		}
		if(exception.get()) {
			Toast.makeText(this, "Unable to send image.", Toast.LENGTH_SHORT).show();
			return null;
		}
		return getBitmapUri(bitmap.get());
	}

	protected boolean requestPermissions(String permission, String rationale) {
		int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
		return permissionCheck == PackageManager.PERMISSION_GRANTED;
	}

	public boolean isPasswordField() {
		EditorInfo info = getCurrentInputEditorInfo();
		switch (info.inputType & EditorInfo.TYPE_MASK_CLASS) {
		case EditorInfo.TYPE_CLASS_TEXT:
			switch (info.inputType & EditorInfo.TYPE_MASK_VARIATION) {
			case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
			case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
			case EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD:
				return true;
			}
		}
		return false;
	}
}
