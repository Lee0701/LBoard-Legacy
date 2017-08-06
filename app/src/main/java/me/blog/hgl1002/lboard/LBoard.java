package me.blog.hgl1002.lboard;

import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import me.blog.hgl1002.lboard.ime.LBoardInputMethod;
import me.blog.hgl1002.lboard.ime.charactergenerator.CharacterGenerator;
import me.blog.hgl1002.lboard.ime.charactergenerator.UnicodeCharacterGenerator;
import me.blog.hgl1002.lboard.ime.softkeyboard.DefaultSoftKeyboard;

import me.blog.hgl1002.lboard.ime.KeyEventInfo;
import me.blog.hgl1002.lboard.ime.hardkeyboard.DefaultHardKeyboard;
import me.blog.hgl1002.lboard.search.DefaultSearchViewManager;
import me.blog.hgl1002.lboard.search.SearchEngine;
import me.blog.hgl1002.lboard.search.SearchViewManager;
import me.blog.hgl1002.lboard.search.engines.GoogleWebSearchEngine;


public class LBoard extends InputMethodService {

	protected ViewGroup mainInputView;
	protected View keyboardView;
	protected View searchView;

	protected SearchViewManager searchViewManager;

	protected List<LBoardInputMethod> inputMethods;
	protected int currentInputMethodId;
	protected LBoardInputMethod currentInputMethod;

	protected Button searchButton;

	Animation slideUp, slideDown;

	private boolean inputted = false;
	private boolean searchViewShown = false;
	private String searchText = "", searchTextComposing = "";

	protected CharacterGenerator.CharacterGeneratorListener characterGeneratorListener
			 = new CharacterGenerator.CharacterGeneratorListener() {
		@Override
		public void onCompose(String composing) {
			if(searchViewShown) {
				searchTextComposing = composing;
				searchViewManager.setText(searchText + searchTextComposing);
			} else {
				getCurrentInputConnection().setComposingText(composing, 1);
			}
		}

		@Override
		public void onCommit() {
			if(searchViewShown) {
				searchText += searchTextComposing;
				searchTextComposing = "";
			} else {
				getCurrentInputConnection().finishComposingText();
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

		softKeyboard.createKeyboards(this, R.xml.keyboard_sebeol_final, R.xml.keyboard_sebeol_final_shift, R.xml.keyboard_lower_default);
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

		softKeyboard.createKeyboards(this, R.xml.keyboard_qwerty, R.xml.keyboard_qwerty, R.xml.keyboard_lower_default);

		LBoardInputMethod qwerty = new LBoardInputMethod("Qwerty", softKeyboard, hardKeyboard, generator);

		inputMethods.add(qwerty);
		inputMethods.add(sebeolFinal);

		currentInputMethod = inputMethods.get(currentInputMethodId);

		SearchEngine engine = new GoogleWebSearchEngine();
		searchViewManager = new DefaultSearchViewManager(this, engine);
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

		searchButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() != MotionEvent.ACTION_DOWN) return false;
				searchViewShown = !searchViewShown;
				if(searchViewShown) {
					v.setVisibility(View.INVISIBLE);
				} else {
					v.setVisibility(View.VISIBLE);
				}
				return false;
			}
		});

		searchView = searchViewManager.createView(this);
		mainInputView.addView(searchView);

		linearLayout.bringToFront();

		return this.mainInputView;
	}

	public void updateInputView() {
		FrameLayout placeholder = (FrameLayout) mainInputView.findViewById(R.id.keyboard_placeholder);
		placeholder.removeAllViews();
		keyboardView = currentInputMethod.getSoftKeyboard().createView(this);
		placeholder.addView(keyboardView);
	}

	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting);
		currentInputMethod.getCharacterGenerator().resetComposing();
	}

	public boolean onKeyEvent(KeyEvent event, boolean hardKey) {
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
						if(searchText.length() > 0) searchText = searchText.substring(0, searchText.length()-1);
						searchViewManager.setText(searchText + searchTextComposing);
					} else {
						getCurrentInputConnection().deleteSurroundingText(1, 0);
					}
				}
			}
			return true;
		}
		inputted = true;
		ret = currentInputMethod.getHardKeyboard().onKeyEvent(
				event, new KeyEventInfo.Builder().setKeyType(hardKey ? KeyEventInfo.KEYTYPE_HARDKEY : KeyEventInfo.KEYTYPE_SOFTKEY).build());

		if(searchViewShown && !ret) {
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

		return ret;
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
		if(object instanceof String) {
			ic.commitText((String) object, 1);
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

	public void finishComposing() {
		currentInputMethod.getCharacterGenerator().resetComposing();
	}

}
