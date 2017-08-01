package me.blog.hgl1002.lboard.ime.softkeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;

import me.blog.hgl1002.lboard.LBoard;
import me.blog.hgl1002.lboard.R;
import me.blog.hgl1002.lboard.ime.KeyEventInfo;
import me.blog.hgl1002.lboard.ime.SoftKeyboard;

public class DefaultSoftKeyboard implements SoftKeyboard, KeyboardView.OnKeyboardActionListener {

	LBoard parent;

	/**
	 * 키보드 뷰들을 포함하는 메인 뷰.
	 */
	protected ViewGroup mainView;
	/**
	 * 메인 키보드 뷰. 예: 한글 키보드, 영문 키보드.
	 */
	protected KeyboardView mainKeyboardView;
	/**
	 * 상단 키보드 뷰. 예: 숫자열 키보드, 커서 이동 키보드.
	 */
	protected KeyboardView upperKeyboardView;
	/**
	 * 하단 키보드 뷰. 예: 스페이스바, 언어 전환 키 등을 포함하는 열.
	 */
	protected KeyboardView lowerKeyboardView;
	/**
	 * 상태 뷰. 예: 한/영 상태 표시기, Shift/Alt 키 표시기.
	 */
	View statusView;

	protected Keyboard currentMainKeyboard;
	protected Keyboard currentUpperKeyboard;
	protected Keyboard currentLowerKeyboard;

	protected boolean shiftPressed;

	public DefaultSoftKeyboard(LBoard parent) {
		this.parent = parent;
	}

	@Override
	public View createView(Context context) {
		LinearLayout mainView = new LinearLayout(context);
		mainView.setOrientation(LinearLayout.VERTICAL);

		mainKeyboardView = new KeyboardView(context, null);
		lowerKeyboardView = new KeyboardView(context, null);

		mainKeyboardView.setOnKeyboardActionListener(this);
		lowerKeyboardView.setOnKeyboardActionListener(this);

		mainView.addView(mainKeyboardView);
		mainView.addView(lowerKeyboardView);

		this.mainView = mainView;

		createKeyboards(context);
		mainKeyboardView.setKeyboard(currentMainKeyboard);
		lowerKeyboardView.setKeyboard(currentLowerKeyboard);

		return mainView;
	}

	public void createKeyboards(Context context) {
		currentMainKeyboard = new Keyboard(context, R.xml.keyboard_qwerty);
		currentLowerKeyboard = new Keyboard(context, R.xml.keyboard_lower);
	}

	@Override
	public View getView() {
		return mainView;
	}

	@Override
	public void setPreferences(SharedPreferences pref, EditorInfo info) {

	}

	@Override
	public void close() {

	}

	@Override
	public void onPress(int primaryCode) {
	}

	@Override
	public void onRelease(int primaryCode) {
	}

	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		switch(primaryCode) {
		case KeyEvent.KEYCODE_SHIFT_LEFT:
		case KeyEvent.KEYCODE_SHIFT_RIGHT:
			shiftPressed = !shiftPressed;
			parent.onKeyEvent(new KeyEvent(shiftPressed ? KeyEvent.ACTION_DOWN : KeyEvent.ACTION_UP, primaryCode), false);
			return;
		}
		KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, primaryCode);
		boolean ret = parent.onKeyEvent(event, false);
		parent.onKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, primaryCode), false);
		if(!ret) parent.getCurrentInputConnection().sendKeyEvent(event);
	}

	@Override
	public void onText(CharSequence text) {

	}

	@Override
	public void swipeLeft() {

	}

	@Override
	public void swipeRight() {

	}

	@Override
	public void swipeDown() {

	}

	@Override
	public void swipeUp() {

	}
}
