package me.blog.hgl1002.lboard;

import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.View;

public class LBoard extends InputMethodService {
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
		return super.onCreateInputView();
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
