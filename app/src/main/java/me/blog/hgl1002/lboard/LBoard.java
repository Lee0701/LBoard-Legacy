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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import me.blog.hgl1002.lboard.cand.CandidatesViewManager;
import me.blog.hgl1002.lboard.cand.TextCandidatesViewManager;
import me.blog.hgl1002.lboard.engine.DictionaryManager;
import me.blog.hgl1002.lboard.engine.LBoardDictionary;
import me.blog.hgl1002.lboard.engine.LBoardPredictionEngine;
import me.blog.hgl1002.lboard.engine.PredictionEngine;
import me.blog.hgl1002.lboard.engine.SQLiteDictionary;
import me.blog.hgl1002.lboard.engine.Sentence;
import me.blog.hgl1002.lboard.engine.StringSegment;
import me.blog.hgl1002.lboard.engine.Word;
import me.blog.hgl1002.lboard.engine.WordChain;
import me.blog.hgl1002.lboard.ime.HardKeyboard;
import me.blog.hgl1002.lboard.ime.InputMethodLoader;
import me.blog.hgl1002.lboard.ime.InternalInputMethodLoader;
import me.blog.hgl1002.lboard.ime.LBoardInputMethod;
import me.blog.hgl1002.lboard.ime.SoftKeyboard;
import me.blog.hgl1002.lboard.ime.charactergenerator.CharacterGenerator;

import me.blog.hgl1002.lboard.ime.KeyEventInfo;
import me.blog.hgl1002.lboard.ime.hardkeyboard.BasicHardKeyboard;
import me.blog.hgl1002.lboard.ime.hardkeyboard.DefaultHardKeyboard;
import me.blog.hgl1002.lboard.ime.softkeyboard.DefaultSoftKeyboard;
import me.blog.hgl1002.lboard.search.DefaultSearchViewManager;
import me.blog.hgl1002.lboard.search.SearchEngine;
import me.blog.hgl1002.lboard.search.SearchViewManager;
import me.blog.hgl1002.lboard.search.data.ImageData;
import me.blog.hgl1002.lboard.search.data.UrlStringData;
import me.blog.hgl1002.lboard.search.engines.GoogleWebSearchEngine;

public class LBoard extends InputMethodService {

	public static final String DIRNAME_METHODS = "methods";

	public static final String DICTIONARY_KO = "ko";
	public static final String DICTIONARY_EN = "en";

	public static final int MSG_UPDATE_CANDIDATES = 1;
	public static final int MSG_UPDATE_PREDICTION = 2;

	public static final int DELAY_DISPLAY_CANDIDATES = 100;

	private static final CharacterStyle SPAN_COMPOSING_WORD = new BackgroundColorSpan(0xFF81DAF5);
	private static final CharacterStyle SPAN_COMPOSING_SENTENCE = new BackgroundColorSpan(0xFFE0F2F7);
	private static final CharacterStyle SPAN_COMPOSING_CHAR = new BackgroundColorSpan(0xFF00BFFF);

	public static final char STROKE_SEPARATOR = '\t';

	protected ViewGroup mainInputView;
	protected View keyboardView;
	protected ViewGroup mainCandidatesView;
	protected View searchView;
	protected View candidatesView;

	protected SearchViewManager searchViewManager;

	protected CandidatesViewManager candidatesViewManager;

	protected PredictionEngine predictionEngine;

	protected List<LBoardInputMethod> inputMethods;
	protected int currentInputMethodId;
	protected LBoardInputMethod currentInputMethod;

	protected Button searchButton;

	Animation slideUp, slideDown;

	protected boolean sentenceUnitComposition = true;

	protected String composingChar = "";

	private boolean inputted = false;
	private boolean searchViewShown = false;
	private String searchText = "", searchTextComposing = "";

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
				updateInput();
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

	public LBoard() {
		inputMethods = new ArrayList<>();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
		slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);

		loadInputMethods();

		currentInputMethod = inputMethods.get(currentInputMethodId);

		SearchEngine engine = new GoogleWebSearchEngine();
		searchViewManager = new DefaultSearchViewManager(this, engine);

		LBoardPredictionEngine predictionEngine = new LBoardPredictionEngine(this);

		LBoardDictionary dictionary = new SQLiteDictionary(getFilesDir() + "/" + DICTIONARY_KO + ".dic");
		predictionEngine.getDictionaryManager().addDictionary(DICTIONARY_KO, dictionary);
		predictionEngine.setDictionaryName(DICTIONARY_KO);
		candidatesViewManager = new TextCandidatesViewManager();
		candidatesViewManager.setListener(predictionEngine.getCandidatesViewListener());

		predictionEngine.start(false);
		this.predictionEngine = predictionEngine;
	}

	public void loadInputMethods() {
		InputMethodLoader loader = new InternalInputMethodLoader(this);
		File file = new File(getFilesDir(), DIRNAME_METHODS);
		if(true) {
			createDefaultMethods();
		}

		for(String fileName : file.list()) {
			File dir = new File(file, fileName);
			if(!dir.isDirectory()) continue;
			File lime = new File(dir, InternalInputMethodLoader.FILENAME_METHOD_DEF);
			if(!lime.exists()) continue;
			LBoardInputMethod method = loader.load(lime);
			method.getCharacterGenerator().setListener(characterGeneratorListener);
			CharSequence[][] labels = new CharSequence[0x100][2];
			SoftKeyboard soft = method.getSoftKeyboard();
			HardKeyboard hard = method.getHardKeyboard();
			if(soft instanceof DefaultSoftKeyboard) {
				if(hard instanceof DefaultHardKeyboard) {
					long[][] mappings = ((DefaultHardKeyboard) hard).getMappings();
					for(int i = 0 ; i < mappings.length ; i++) {
						for(int j = 0 ; j < 2 ; j++) {
							try {
								labels[i][j] = new String(Character.toChars((int) mappings[i][j]));
							} catch(Exception e) {
								labels[i][j] = null;
							}
						}
					}
					((DefaultSoftKeyboard) soft).setLabels(labels);
				}
			}
			inputMethods.add(method);
		}

	}

	public void createDefaultMethods() {
		try {
			File file = new File(getFilesDir(), DIRNAME_METHODS);
			{
				File qwerty = new File(file, "Qwerty");
				qwerty.mkdirs();
				File lime = new File(qwerty, "method.lime");
				FileOutputStream fos = new FileOutputStream(lime);
				DataOutputStream dos = new DataOutputStream(fos);
				for(char c : InternalInputMethodLoader.MAGIC_NUMBER.toCharArray()) {
					dos.writeByte((byte) c);
				}
				for(char c : "Qwerty".toCharArray()) {
					dos.writeByte((byte) c);
				}
				dos.writeByte(0);
				dos.writeByte(InternalInputMethodLoader.SOFT_DEFAULT);
				dos.writeByte(InternalInputMethodLoader.HARD_DEFAULT);
				dos.writeByte(InternalInputMethodLoader.CG_UNICODE);
				for(char c : DICTIONARY_KO.toCharArray()) {
					dos.writeByte((byte) c);
				}
				dos.writeByte(0);

				Properties properties = new Properties();
				properties.setProperty(InternalInputMethodLoader.KEY_DEFAULT_SOFT_MAIN, "keyboard_qwerty_4rows");
				properties.setProperty(InternalInputMethodLoader.KEY_DEFAULT_SOFT_MAIN_SHIFT, "keyboard_qwerty_4rows");
				properties.setProperty(InternalInputMethodLoader.KEY_DEFAULT_SOFT_LOWER, "keyboard_lower_default");
				properties.store(new FileOutputStream(new File(qwerty, InternalInputMethodLoader.FILENAME_DEFAULT_SOFT_DEF)), "");

				InputStream is = getResources().openRawResource(R.raw.layout_qwerty);
				byte[] data = new byte[is.available()];
				is.read(data);
				fos = new FileOutputStream(new File(qwerty, InternalInputMethodLoader.FILENAME_DEFAULT_HARD));
				fos.write(data);
			}
			{
				File sebeolFinal = new File(file, "Shin Sebeol 1995 Original");
				sebeolFinal.mkdirs();
				File lime = new File(sebeolFinal, "method.lime");
				FileOutputStream fos = new FileOutputStream(lime);
				DataOutputStream dos = new DataOutputStream(fos);
				for(char c : InternalInputMethodLoader.MAGIC_NUMBER.toCharArray()) {
					dos.writeByte((byte) c);
				}
				for(char c : "Shin Sebeol 1995 Original".toCharArray()) {
					dos.writeByte((byte) c);
				}
				dos.writeByte(0);
				dos.writeByte(InternalInputMethodLoader.SOFT_DEFAULT);
				dos.writeByte(InternalInputMethodLoader.HARD_BASIC);
				dos.writeByte(InternalInputMethodLoader.CG_BASIC);
				for(char c : DICTIONARY_KO.toCharArray()) {
					dos.writeByte((byte) c);
				}
				dos.writeByte(0);

				Properties properties = new Properties();
				properties.setProperty(InternalInputMethodLoader.KEY_DEFAULT_SOFT_MAIN, "keyboard_full_10cols");
				properties.setProperty(InternalInputMethodLoader.KEY_DEFAULT_SOFT_MAIN_SHIFT, "keyboard_full_10cols");
				properties.setProperty(InternalInputMethodLoader.KEY_DEFAULT_SOFT_LOWER, "keyboard_lower_default");
				properties.store(new FileOutputStream(new File(sebeolFinal, InternalInputMethodLoader.FILENAME_DEFAULT_SOFT_DEF)), "");

				InputStream is = getResources().openRawResource(R.raw.layout_shin_1995);
				byte[] data = new byte[is.available()];
				is.read(data);
				fos = new FileOutputStream(new File(sebeolFinal, InternalInputMethodLoader.FILENAME_BASIC_HARD));
				fos.write(data);

				is = getResources().openRawResource(R.raw.comb_shin_3);
				data = new byte[is.available()];
				is.read(data);
				fos = new FileOutputStream(new File(sebeolFinal, InternalInputMethodLoader.FILENAME_COMBINATION));
				fos.write(data);

				is = getResources().openRawResource(R.raw.virtual_shin);
				data = new byte[is.available()];
				is.read(data);
				fos = new FileOutputStream(new File(sebeolFinal, InternalInputMethodLoader.FILENAME_VIRTUAL));
				fos.write(data);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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

		for(LBoardInputMethod method : inputMethods) {
			if(method.getSoftKeyboard() instanceof DefaultSoftKeyboard) {
				((DefaultSoftKeyboard) method.getSoftKeyboard()).updateLabels();
			}
		}

		candidatesView = candidatesViewManager.createView(this);
		mainInputView.addView(candidatesView);

		linearLayout.bringToFront();

		setCandidatesViewShown(true);

		return this.mainInputView;
	}

	public void updateInput() {
		if(sentenceUnitComposition) {
			getCurrentInputConnection().setComposingText(predictionEngine.getComposing(), 1);
		} else {
			getCurrentInputConnection().setComposingText(composingChar, 1);
		}
	}

	public void updateInputView() {
		FrameLayout placeholder = (FrameLayout) mainInputView.findViewById(R.id.keyboard_placeholder);
		placeholder.removeAllViews();
		keyboardView = currentInputMethod.getSoftKeyboard().createView(this);
		if(currentInputMethod.getHardKeyboard() instanceof BasicHardKeyboard) {
			((BasicHardKeyboard) currentInputMethod.getHardKeyboard()).updateLabels();
		}
		placeholder.addView(keyboardView);
	}

	@Override
	public void onStartInputView(EditorInfo info, boolean restarting) {
		super.onStartInputView(info, restarting);
		if(sentenceUnitComposition) {
			predictionEngine.start(restarting);
		} else {
			composingChar = "";
			commitComposingChar();
		}
	}

	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting);
		composingChar = "";
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
				predictionEngine.halfCommitWord();

				if(++currentInputMethodId >= inputMethods.size()) currentInputMethodId = 0;
				currentInputMethod = inputMethods.get(currentInputMethodId);
				updateInputView();

				updateInput();
			}
			return true;
		case KeyEvent.KEYCODE_DEL:
			if(event.getAction() == KeyEvent.ACTION_DOWN) {
				if(sentenceUnitComposition) {
					if(!predictionEngine.backspace()) {
						if(searchViewShown) {
							if(searchText.length() > 0) {
								searchText = searchText.substring(0, searchText.length()-1);
							}
							searchViewManager.setText(searchText + searchTextComposing);
						}
					}
				} else {
					if(!backspace()) {
						if(searchViewShown) {
							if(searchText.length() > 0) {
								searchText = searchText.substring(0, searchText.length()-1);
							}
							searchViewManager.setText(searchText + searchTextComposing);
						}
					}
				}
			}
			return true;
		}

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
				if(sentenceUnitComposition) {

				} else {
					return false;
				}
				return true;

			case KeyEvent.KEYCODE_ENTER:
				if(sentenceUnitComposition) {

				} else {
					return false;
				}
				break;

			}
		}
		inputted = true;
		ret = currentInputMethod.getHardKeyboard().onKeyEvent(
				event, new KeyEventInfo.Builder().setKeyType(hardKey ? KeyEventInfo.KEYTYPE_HARDKEY : KeyEventInfo.KEYTYPE_SOFTKEY).build());

		if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == -114) {
			shareDictionary();
		}

		return ret;
	}

	public boolean backspace() {
		if (currentInputMethod.getCharacterGenerator().backspace()) {
			return true;
		}
		return false;
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
					if(sentenceUnitComposition) {
						predictionEngine.appendText(text);
					} else {
						getCurrentInputConnection().commitText(text, 1);
					}
				}
				break;

			default:
				getCurrentInputConnection().commitText(text, 1);
			}
		}
	}

	public void composeChar(String composingChar) {
		if(sentenceUnitComposition) {
			predictionEngine.composeChar(composingChar);
		} else {
			this.composingChar = composingChar;
		}
	}

	public void commitComposingChar() {
		if(sentenceUnitComposition) {
			predictionEngine.commitComposingChar();
		} else {
			InputConnection ic = getCurrentInputConnection();
			ic.setComposingText(composingChar, 1);
			ic.finishComposingText();
			composingChar = "";
			return;
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
