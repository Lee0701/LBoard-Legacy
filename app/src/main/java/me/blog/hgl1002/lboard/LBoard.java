package me.blog.hgl1002.lboard;

import android.inputmethodservice.InputMethodService;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import me.blog.hgl1002.lboard.ime.SoftKeyboard;
import me.blog.hgl1002.lboard.ime.charactergenerator.CharacterGenerator;
import me.blog.hgl1002.lboard.ime.charactergenerator.UnicodeCharacterGenerator;
import me.blog.hgl1002.lboard.ime.softkeyboard.DefaultSoftKeyboard;

import me.blog.hgl1002.lboard.ime.HardKeyboard;
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

	protected Button searchButton;

	protected SoftKeyboard softKeyboard;
	protected HardKeyboard hardKeyboard;
	protected CharacterGenerator characterGenerator;

	protected SearchViewManager searchViewManager;

	Animation slideUp, slideDown;

	private boolean inputted = false;
	private boolean searchViewShown = false;
	private String searchText = "", searchTextComposing = "";

	public LBoard() {
	}

	@Override
	public void onCreate() {
		super.onCreate();

		slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
		slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);

		DefaultSoftKeyboard softKeyboard = new DefaultSoftKeyboard(this);
		DefaultHardKeyboard hardKeyboard = new DefaultHardKeyboard(this);
		UnicodeCharacterGenerator generator = new UnicodeCharacterGenerator();
		generator.setListener(new CharacterGenerator.CharacterGeneratorListener() {
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
		});
		generator.setCombinationTable(UnicodeCharacterGenerator.loadCombinationTable(getResources().openRawResource(R.raw.comb_sebeol)));
		hardKeyboard.setCharacterGenerator(generator);
		hardKeyboard.setMappings(DefaultHardKeyboard.loadMappings(getResources().openRawResource(R.raw.layout_sebeol_final)));
		this.softKeyboard = softKeyboard;
		this.hardKeyboard = hardKeyboard;
		this.characterGenerator = generator;

		SearchEngine engine = new GoogleWebSearchEngine();
		searchViewManager = new DefaultSearchViewManager(this, engine);
	}

	@Override
	public View onCreateCandidatesView() {
		return super.onCreateCandidatesView();
	}

	@Override
	public View onCreateInputView() {
		mainInputView = new FrameLayout(this);
		LinearLayout linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);

		searchButton = new Button(this);
		linearLayout.addView(searchButton);

		keyboardView = softKeyboard.createView(this);
		linearLayout.addView(keyboardView);

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

		mainInputView.addView(linearLayout);
		return this.mainInputView;
	}

	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting);
		characterGenerator.resetComposing();
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
				InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				IBinder token = getWindow().getWindow().getAttributes().token;
				if(inputted) {
					manager.switchToLastInputMethod(token);
				} else {
					manager.switchToNextInputMethod(token, false);
				}
			}
			return true;
		case KeyEvent.KEYCODE_DEL:
			if(event.getAction() == KeyEvent.ACTION_DOWN) {
				if (!characterGenerator.backspace()) {
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
		ret = hardKeyboard.onKeyEvent(
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
		characterGenerator.resetComposing();
	}

}
