package me.blog.hgl1002.lboard;

import android.inputmethodservice.InputMethodService;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import me.blog.hgl1002.lboard.ime.SoftKeyboard;
import me.blog.hgl1002.lboard.ime.charactergenerator.CharacterGenerator;
import me.blog.hgl1002.lboard.ime.charactergenerator.UnicodeCharacterGenerator;
import me.blog.hgl1002.lboard.ime.softkeyboard.DefaultSoftKeyboard;

import me.blog.hgl1002.lboard.ime.HardKeyboard;
import me.blog.hgl1002.lboard.ime.KeyEventInfo;
import me.blog.hgl1002.lboard.ime.hardkeyboard.DefaultHardKeyboard;


public class LBoard extends InputMethodService {

	protected SoftKeyboard softKeyboard;
	protected HardKeyboard hardKeyboard;
	protected CharacterGenerator characterGenerator;

	private boolean inputted = false;

	public LBoard() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
		DefaultSoftKeyboard softKeyboard = new DefaultSoftKeyboard(this);
		DefaultHardKeyboard hardKeyboard = new DefaultHardKeyboard(this);
		UnicodeCharacterGenerator generator = new UnicodeCharacterGenerator();
		generator.setListener(new CharacterGenerator.CharacterGeneratorListener() {
			@Override
			public void onCompose(String composing) {
				getCurrentInputConnection().setComposingText(composing, 1);
			}

			@Override
			public void onCommit() {
				getCurrentInputConnection().finishComposingText();
			}
		});
		generator.setCombinationTable(UnicodeCharacterGenerator.loadCombinationTable(getResources().openRawResource(R.raw.comb_sebeol)));
		hardKeyboard.setCharacterGenerator(generator);
		hardKeyboard.setMappings(DefaultHardKeyboard.loadMappings(getResources().openRawResource(R.raw.layout_sebeol_final)));
		this.softKeyboard = softKeyboard;
		this.hardKeyboard = hardKeyboard;
		this.characterGenerator = generator;
	}

	@Override
	public View onCreateCandidatesView() {
		return super.onCreateCandidatesView();
	}

	@Override
	public View onCreateInputView() {
		return softKeyboard.createView(this);
	}

	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting);
		characterGenerator.resetComposing();
	}

	public boolean onKeyEvent(KeyEvent event, boolean hardKey) {
		boolean ret = false;
		switch(event.getKeyCode()) {
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
					getCurrentInputConnection().deleteSurroundingText(1, 0);
				}
			}
			return true;
		}
		inputted = true;
		ret = hardKeyboard.onKeyEvent(
				event, new KeyEventInfo.Builder().setKeyType(hardKey ? KeyEventInfo.KEYTYPE_HARDKEY : KeyEventInfo.KEYTYPE_SOFTKEY).build());
		return ret;
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
		getCurrentInputConnection().commitText(text, 1);
	}

	public void finishComposing() {
		characterGenerator.resetComposing();
	}

}
