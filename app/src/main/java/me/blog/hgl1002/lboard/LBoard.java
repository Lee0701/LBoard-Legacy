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
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import me.blog.hgl1002.lboard.cand.CandidatesViewManager;
import me.blog.hgl1002.lboard.cand.TextCandidatesViewManager;
import me.blog.hgl1002.lboard.engine.LBoardDictionary;
import me.blog.hgl1002.lboard.engine.SQLiteDictionary;
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

	public static final int DELAY_DISPLAY_CANDIDATES = 100;

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

	private String composingWord;
	private String composingChar;

	private String currentWord;
	private String previousWord;
	private WordChain chain;
	private boolean start;

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_CANDIDATES:
				List<Word> candidatesList = new ArrayList<>();
				Word[] previousWords = Arrays.copyOfRange(chain.getAll(), 1, chain.getAll().length);
				Word[] candidates = dictionary.searchNextWord(LBoardDictionary.SEARCH_CHAIN, LBoardDictionary.ORDER_BY_FREQUENCY, previousWord, previousWords);
				Collections.addAll(candidatesList, candidates);
				if(start) {
					previousWords = new Word[] {WordChain.START, WordChain.START};
					candidates = dictionary.searchNextWord(LBoardDictionary.SEARCH_CHAIN, LBoardDictionary.ORDER_BY_FREQUENCY, previousWord, previousWords);
					Collections.addAll(candidatesList, candidates);
				}
				candidates = new Word[candidatesList.size()];
				candidatesViewManager.setCandidates(candidatesList.toArray(candidates));
				break;
			}
		}
	};

	protected CharacterGenerator.CharacterGeneratorListener characterGeneratorListener
			 = new CharacterGenerator.CharacterGeneratorListener() {
		@Override
		public void onCompose(String composing) {
			InputConnection ic = getCurrentInputConnection();
			if(ic == null) return;
			if(searchViewShown) {
				searchTextComposing = composing;
				searchViewManager.setText(searchText + searchTextComposing);
			} else {
				composeChar(composing);
				updateInput();
			}
		}

		@Override
		public void onCommit() {
			InputConnection ic = getCurrentInputConnection();
			if(ic == null) return;
			if(searchViewShown) {
				searchText += searchTextComposing;
				searchTextComposing = "";
			} else {
				commitComposing();
				updateInput();
			}
		}
	};

	protected CandidatesViewManager.CandidatesViewListener candidatesViewListener
			 = new CandidatesViewManager.CandidatesViewListener() {
		@Override
		public void onSelect(Object candidate) {
			composeWord(((String) candidate));
			updateInput();
			commitWord(true);
			commitText(" ");
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

		resetComposing();
		chain = new WordChain(new Word[] {WordChain.START, WordChain.START, WordChain.START});
		currentWord = "";
		previousWord = "";

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

	public void updateCandidates() {
		handler.sendMessageDelayed(handler.obtainMessage(MSG_UPDATE_CANDIDATES), DELAY_DISPLAY_CANDIDATES);
	}

	public void updateInput() {
		InputConnection ic = getCurrentInputConnection();
		currentWord = composingWord + composingChar;
		ic.setComposingText(currentWord, 1);
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
			learnWord(new Word(currentWord, null), start, true);
			previousWord = currentWord;
			resetComposing();
			updateInput();
			start = true;
		} else {
			resetComposing();
			chain = new WordChain(new Word[] {WordChain.START, WordChain.START, WordChain.START});
			currentWord = "";
			previousWord = "";
		}
		updateCandidates();
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
							composingWord = composingWord.substring(0, composingWord.length() - 1);
							updateInput();
						} else {
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
				commitWord(true);
				break;

			case KeyEvent.KEYCODE_ENTER:
			case KeyEvent.KEYCODE_PERIOD:
				commitWord(true);
				resetComposing();
				break;

			}
		}
		if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == -114) {
			shareDictionary();
		}
		return ret;
	}

	public void resetComposing() {
		composingWord = "";
		composingChar = "";
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

	public void commitText(CharSequence text) {
		if(searchViewShown) {
			searchText += text;
			searchViewManager.setText(searchText + searchTextComposing);
		} else {
			getCurrentInputConnection().commitText(text, 1);
		}
	}

	public void composeChar(String composing) {
		composingChar = composing;
	}

	public void composeWord(String word) {
		commitComposing();
		composingWord = word;
	}

	public void commitComposing() {
		composingWord += composingChar;
		composingChar = "";
	}

	public void commitWord(boolean learn) {
		commitComposing();
		if(learn) {
			Word word = new Word(currentWord, null);
			if(start) learnWord(word, true, false);
			learnWord(word, false, false);
			start = false;
		}
		InputConnection ic = getCurrentInputConnection();
		ic.setComposingText(currentWord, 1);
		ic.finishComposingText();
		previousWord = currentWord;
		composingWord = "";
		currentWord = "";
		updateCandidates();
	}

	public void learnWord(Word word, boolean start, boolean end) {
		if(dictionary instanceof SQLiteDictionary && currentWord != "") {
			SQLiteDictionary dictionary = (SQLiteDictionary) this.dictionary;
			if(start || end) {
				WordChain chain;
				if(start && end) {
					chain = new WordChain(new Word[] {WordChain.START, word, WordChain.END});
				} else if(start) {
					chain = new WordChain(new Word[] {WordChain.START, WordChain.START, word});
				} else {
					chain = new WordChain(new Word[] {word, WordChain.END, WordChain.END});
				}
				dictionary.learn(chain);
			}
			WordChain prev = this.chain;
			if(prev == null) prev = new WordChain(new Word[] {WordChain.START, WordChain.START, WordChain.START});
			WordChain chain = new WordChain(new Word[] {prev.get(1), prev.get(2), word});
			dictionary.learn(chain);
			this.chain = chain;
		}
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
