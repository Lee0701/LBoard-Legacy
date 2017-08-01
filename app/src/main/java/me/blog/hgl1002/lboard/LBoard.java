package me.blog.hgl1002.lboard;

import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.View;

import me.blog.hgl1002.lboard.ime.SoftKeyboard;
import me.blog.hgl1002.lboard.ime.softkeyboard.DefaultSoftKeyboard;

public class LBoard extends InputMethodService {

	protected SoftKeyboard softKeyboard;

	@Override
	public void onCreate() {
		super.onCreate();
		softKeyboard = new DefaultSoftKeyboard();
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return super.onKeyUp(keyCode, event);
	}

}
