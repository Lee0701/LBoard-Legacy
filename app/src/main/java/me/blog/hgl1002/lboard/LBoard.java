package me.blog.hgl1002.lboard;

import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.View;

import me.blog.hgl1002.lboard.ime.SoftKeyboard;
import me.blog.hgl1002.lboard.ime.softkeyboard.DefaultSoftKeyboard;

import me.blog.hgl1002.lboard.ime.HardKeyboard;
import me.blog.hgl1002.lboard.ime.KeyEventInfo;
import me.blog.hgl1002.lboard.ime.hardkeyboard.DefaultHardKeyboard;


public class LBoard extends InputMethodService {

	protected SoftKeyboard softKeyboard;
	protected HardKeyboard hardKeyboard;

	public LBoard() {
		softKeyboard = new DefaultSoftKeyboard(this);
		hardKeyboard = new DefaultHardKeyboard(this);
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public View onCreateCandidatesView() {
		return super.onCreateCandidatesView();
	}

	@Override
	public View onCreateInputView() {
		return softKeyboard.createView(this);
	}

	public boolean onKeyEvent(KeyEvent event, boolean hardKey) {
		boolean ret = false;
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

}
