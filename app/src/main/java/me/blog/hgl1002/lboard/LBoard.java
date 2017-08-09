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
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import me.blog.hgl1002.lboard.cand.CandidatesViewManager;
import me.blog.hgl1002.lboard.cand.TextCandidatesViewManager;
import me.blog.hgl1002.lboard.engine.ComposingText;
import me.blog.hgl1002.lboard.engine.LBoardDictionary;
import me.blog.hgl1002.lboard.engine.SQLiteDictionary;
import me.blog.hgl1002.lboard.engine.Sentence;
import me.blog.hgl1002.lboard.engine.Word;
import me.blog.hgl1002.lboard.engine.WordChain;
import me.blog.hgl1002.lboard.ime.LBoardInputMethod;
import me.blog.hgl1002.lboard.ime.charactergenerator.CharacterGenerator;
import me.blog.hgl1002.lboard.ime.charactergenerator.UnicodeCharacterGenerator;
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

	public static final int MSG_UPDATE_CANDIDATES = 1;
	public static final int MSG_UPDATE_PREDICTION = 2;

	public static final int DELAY_DISPLAY_CANDIDATES = 100;

	private static final CharacterStyle SPAN_COMPOSING_WORD = new BackgroundColorSpan(0xFFF0FFFF);
	private static final CharacterStyle SPAN_COMPOSING_SENTENCE = new BackgroundColorSpan(0xFF66CDAA);
	private static final CharacterStyle SPAN_COMPOSING_CHAR = new BackgroundColorSpan(0xFF8888FF);

	protected ViewGroup mainInputView;
	protected View keyboardView;
	protected View searchView;

	protected SearchViewManager searchViewManager;

	protected LBoardDictionary dictionary;

	protected CandidatesViewManager candidatesViewManager;

	protected List<LBoardInputMethod> inputMethods;
	protected int currentInputMethodId;
	protected LBoardInputMethod currentInputMethod;

	protected Button searchButton;

	Animation slideUp, slideDown;

	private boolean inputted = false;
	private boolean searchViewShown = false;
	private String searchText = "", searchTextComposing = "";

	private String composingChar;
	private String composingWord;
	private String composingCharStroke;
	private String composingWordStroke;
	private Stack<String> composingWordStrokeHistory = new Stack<>();
	private Sentence sentence;

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Word[] candidates;
			switch (msg.what) {
			case MSG_UPDATE_PREDICTION:
				WordChain chain = getCurrentChain();
				candidates = dictionary.searchNextWord(LBoardDictionary.SEARCH_CHAIN, LBoardDictionary.ORDER_BY_FREQUENCY, composingWord, chain.getAll());
				candidatesViewManager.setCandidates(candidates);
				break;

			case MSG_UPDATE_CANDIDATES:
				String stroke = composingWordStroke + composingCharStroke;
				candidates = dictionary.searchCurrentWord(LBoardDictionary.SEARCH_PREFIX, LBoardDictionary.ORDER_BY_FREQUENCY, stroke);
				candidatesViewManager.setCandidates(candidates);
				break;
			}
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

		slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
		slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);

		DefaultSoftKeyboard softKeyboard = new DefaultSoftKeyboard(this);
		DefaultHardKeyboard hardKeyboard = new DefaultHardKeyboard(this);
		UnicodeCharacterGenerator generator = new UnicodeCharacterGenerator();
		generator.setListener(characterGeneratorListener);
		generator.setCombinationTable(UnicodeCharacterGenerator.loadCombinationTable(getResources().openRawResource(R.raw.comb_sebeol)));
		hardKeyboard.setCharacterGenerator(generator);
		hardKeyboard.setMappings(DefaultHardKeyboard.loadMappings(getResources().openRawResource(R.raw.layout_sebeol_final)));

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

		LBoardInputMethod sebeolFinal = new LBoardInputMethod("Sebeolsik Final", softKeyboard, hardKeyboard, generator);

		softKeyboard = new DefaultSoftKeyboard(this);
		hardKeyboard = new DefaultHardKeyboard(this);
		generator = new UnicodeCharacterGenerator();
		generator.setListener(characterGeneratorListener);
		hardKeyboard.setMappings(DefaultHardKeyboard.loadMappings(getResources().openRawResource(R.raw.layout_qwerty)));

		softKeyboard.createKeyboards(this, R.xml.keyboard_qwerty_4rows, R.xml.keyboard_qwerty_4rows, R.xml.keyboard_lower_default);

		LBoardInputMethod qwerty = new LBoardInputMethod("Qwerty", softKeyboard, hardKeyboard, generator);

		inputMethods.add(qwerty);
		inputMethods.add(sebeolFinal);

		currentInputMethod = inputMethods.get(currentInputMethodId);

		SearchEngine engine = new GoogleWebSearchEngine();
		searchViewManager = new DefaultSearchViewManager(this, engine);

		dictionary = new SQLiteDictionary(getFilesDir() + "/dictionary.dic");

		candidatesViewManager = new TextCandidatesViewManager();
		candidatesViewManager.setListener(candidatesViewListener);

		clearComposing();

		startNewSentence(null);

		updatePrediction();
	}

	@Override
	public View onCreateCandidatesView() {
		View candidatesView = candidatesViewManager.createView(this);
		return candidatesView;
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
					v.setVisibility(View.INVISIBLE);
				} else {
					v.setVisibility(View.VISIBLE);
				}
			}
		});

		searchView = searchViewManager.createView(this);
		mainInputView.addView(searchView);

		linearLayout.bringToFront();

		setCandidatesViewShown(true);

		return this.mainInputView;
	}

	public void updatePrediction() {
		handler.sendMessageDelayed(handler.obtainMessage(MSG_UPDATE_PREDICTION), DELAY_DISPLAY_CANDIDATES);
	}

	public void updateCandidates() {
		handler.sendMessageDelayed(handler.obtainMessage(MSG_UPDATE_CANDIDATES), DELAY_DISPLAY_CANDIDATES);
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
			startNewSentence(sentence);
			clearComposing();
			updateInput();
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
				if(++currentInputMethodId >= 2) currentInputMethodId = 0;
				currentInputMethod = inputMethods.get(currentInputMethodId);
				updateInputView();
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
						if(composingWord.length() > 0) {
							composingWord = composingWord.substring(0, composingWord.length()-1);
							if(composingWordStrokeHistory.isEmpty()) {
								composingWordStroke = "";
							} else {
								composingWordStroke = composingWordStrokeHistory.pop();
							}
							updateInput();
							updateCandidates();
						} else if(sentence.size() > 0) {
							Word word = sentence.pop();
							this.composingWord = word.getCandidate();
							this.composingWordStroke = word.getStroke();
							updateInput();
							updateCandidates();
						} else {
							clearComposing();
							ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
							ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
						}
					}
				}
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
				appendWord(composingWord, composingWordStroke);
				clearComposing();
				updateInput();
				updatePrediction();
				return true;

			case KeyEvent.KEYCODE_ENTER:
			case KeyEvent.KEYCODE_PERIOD:
				commitComposingChar();
				if(!composingWord.isEmpty()) appendWord(composingWord, composingWordStroke);
				clearComposing();
				commitSentence(sentence, true);
				startNewSentence(sentence);
				updateInput();
				updatePrediction();
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
			FileInputStream fis = new FileInputStream(new File(getFilesDir(), "dictionary.dic"));
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
		keyboardView.setVisibility(View.VISIBLE);
		keyboardView.startAnimation(slideUp);
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
			composingWordStrokeHistory.push(composingWordStroke);
			composingWord += text;
			composingWordStroke += text;
			updateInput();
		}
	}

	public void composeChar(String composingChar) {
		this.composingChar = composingChar;
	}

	public void commitComposingChar() {
		this.composingWord += composingChar;
		this.composingChar = "";
		if(composingWordStrokeHistory.isEmpty() || composingWordStrokeHistory.peek() != composingWordStroke)
			composingWordStrokeHistory.push(composingWordStroke);
		this.composingWordStroke += composingCharStroke;
		this.composingCharStroke = "";
	}

	public void appendWord(String composingWord, String ComposingWordStroke) {
		Word word = new Word(composingWord, composingWordStroke, 1);
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
		if(str.length() > 0) str.append(" ");
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
		InputConnection ic = getCurrentInputConnection();

		ic.setComposingText("", 1);
		ic.finishComposingText();

		ic.commitText(sentence.getCandidate(), 1);
	}

	public void learnWord(Word word) {
		if(dictionary instanceof SQLiteDictionary && word.getCandidate() != "") {
			SQLiteDictionary dictionary = (SQLiteDictionary) this.dictionary;
			dictionary.learnWord(word);
		}
	}

	public void learnWordChain(WordChain prev) {
		if(dictionary instanceof SQLiteDictionary) {
			SQLiteDictionary dictionary = (SQLiteDictionary) this.dictionary;
			dictionary.learnChain(prev);
		}
	}

	public WordChain getCurrentChain() {
		if(sentence.getPrev() != null) System.out.println("prev " + sentence.getPrev().size());
		Word[] words = new Word[WordChain.DEFAULT_LENGTH];
		for(int i = 0 ; i < words.length ; i++) {
			int index = words.length - i - 1;
			if(sentence.size()-1 - i < 0) {
				System.out.println(i);
				if(sentence.getPrev() != null && sentence.getPrev().size()-1 + sentence.size() - i >= 0) {
					Sentence prev = sentence.getPrev();
					words[index] = prev.get(prev.size()-1 + sentence.size() - i);
				} else {
					words[index] = WordChain.START;
				}
			} else {
				words[index] = sentence.get(sentence.size()-1 - i);
			}
		}
		System.out.println();
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

}
